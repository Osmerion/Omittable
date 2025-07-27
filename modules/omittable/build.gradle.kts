/*
 * Copyright 2025 Leon Linhart
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
import com.osmerion.build.tasks.TransformIntrinsicsTask
import groovy.util.Node
import groovy.util.NodeList
import org.jetbrains.kotlin.gradle.ExperimentalWasmDsl
import org.jetbrains.kotlin.gradle.dsl.JvmTarget
import org.jetbrains.kotlin.gradle.dsl.KotlinVersion
import org.jetbrains.kotlin.gradle.plugin.mpp.*
import org.jetbrains.kotlin.gradle.targets.jvm.*
import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    alias(buildDeps.plugins.binary.compatibility.validator)
    alias(buildDeps.plugins.dokka)
//    alias(buildDeps.plugins.dokka.javadoc)
    alias(buildDeps.plugins.kotlin.multiplatform)
    alias(buildDeps.plugins.kotlin.plugin.serialization)
    id("com.osmerion.java-base-conventions")
    id("com.osmerion.maven-publish-conventions")
}

kotlin {
    explicitApi()
    applyDefaultHierarchyTemplate()

    compilerOptions {
        apiVersion = KotlinVersion.KOTLIN_2_2
        languageVersion = KotlinVersion.KOTLIN_2_2

        freeCompilerArgs.add("-Xexpect-actual-classes")
    }

    js {
        browser()
        nodejs()
    }

    jvm {
        compilerOptions {
            jvmTarget = JvmTarget.JVM_17
            freeCompilerArgs.add("-Xjdk-release=17")
        }

        compilations {
            named("main") {
                compileJavaTaskProvider!!.configure {
                    options.javaModuleVersion = "$version"
                    options.release = 17

                    options.compilerArgumentProviders += object : CommandLineArgumentProvider {

                        @InputFiles
                        @PathSensitive(PathSensitivity.RELATIVE)
                        val kotlinClasses = project.tasks.named<KotlinCompile>("compileKotlinJvm").flatMap(KotlinCompile::destinationDirectory)

                        override fun asArguments() = listOf(
                            "--patch-module",
                            "com.osmerion.omittable=${kotlinClasses.get().asFile.absolutePath}"
                        )

                    }
                }
            }

            named("test") {
                compileJavaTaskProvider!!.configure {
                    options.release = 21
                }

                compileTaskProvider.configure {
                    compilerOptions {
                        jvmTarget = JvmTarget.JVM_21
                        freeCompilerArgs.set(listOf("-Xjdk-release=21"))
                    }
                }
            }
        }
    }

    androidNativeArm32()
    androidNativeArm64()
    androidNativeX64()
    androidNativeX86()

    linuxArm64()
    linuxX64()

    iosArm64()
    iosX64()

    iosSimulatorArm64()

    macosArm64()
    macosX64()

    mingwX64()

    tvosArm64()
    tvosX64()

    tvosSimulatorArm64()

    @OptIn(ExperimentalWasmDsl::class)
    wasmJs {
        browser()
        nodejs()
    }

    @OptIn(ExperimentalWasmDsl::class)
    wasmWasi {
        nodejs()
    }

    watchosArm32()
    watchosArm64()
    watchosX64()

    watchosDeviceArm64()
    watchosSimulatorArm64()

    sourceSets {
        commonMain {
            dependencies {
                compileOnly(libs.kotlinx.serialization.core)
            }
        }

        commonTest {
            dependencies {
                implementation(buildDeps.kotlin.test)
                implementation(libs.kotlinx.serialization.json)
            }
        }

        jvmTest {
            dependencies {
                implementation(project.dependencies.platform(buildDeps.junit.bom))
                implementation(buildDeps.assertj.core)
                implementation(buildDeps.junit.jupiter.api)
                implementation(buildDeps.kotlin.test.junit5)

                runtimeOnly(buildDeps.junit.jupiter.engine)
                runtimeOnly(buildDeps.junit.platform.launcher)
            }
        }

        val nonJvmMain by registering {
            dependsOn(commonMain.get())

            dependencies {
                api(kotlin("stdlib"))
                api(libs.kotlinx.serialization.core)
            }
        }

        jsMain {
            dependsOn(nonJvmMain.get())
        }

        nativeMain {
            dependsOn(nonJvmMain.get())
        }

        wasmJsMain {
            dependsOn(nonJvmMain.get())
        }

        wasmWasiMain {
            dependsOn(nonJvmMain.get())
        }
    }

    targets.filter { it is KotlinJvmTarget || it is KotlinWithJavaTarget<*, *> }.forEach { target ->
        tasks.named<Jar>(target.artifactsTaskName) {
            manifest {
                attributes(mapOf(
                    "Name" to project.name,
                    "Specification-Version" to project.version,
                    "Specification-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>",
                    "Implementation-Version" to project.version,
                    "Implementation-Vendor" to "Leon Linhart <themrmilchmann@gmail.com>"
                ))
            }
        }
    }
}

dokka {
    dokkaGeneratorIsolation = ProcessIsolation {
        maxHeapSize = "4G"
    }

    dokkaSourceSets.configureEach {
        reportUndocumented = true
        skipEmptyPackages = true
        jdkVersion = 17

        val localKotlinSourceDir = layout.projectDirectory.dir("src/$name/kotlin")
        val version = project.version

        sourceLink {
            localDirectory = localKotlinSourceDir

            remoteUrl("https://github.com/Osmerion/Omittable/tree/v${version}/src/main/kotlin")
            remoteLineSuffix = "#L"
        }
    }

    dokkaSourceSets {
        commonMain {
            samples.from(files("src/commonTest/kotlin"))
        }

        named("jvmMain") {
            samples.from(files("src/jvmTest/kotlin"))
        }
    }

    dokkaPublications.configureEach {
        moduleName = "Omittable"

        // TODO Enable this once there are fewer silly warnings for missing docs on inherited functions
//        failOnWarning = true
    }
}

tasks {
    withType<Test>().configureEach {
        useJUnitPlatform()
    }

    withType<Jar>().configureEach {
        isPreserveFileTimestamps = false
        isReproducibleFileOrder = true

        includeEmptyDirs = false
    }

    // See https://github.com/GW2ToolBelt/GW2ChatLinks/issues/18
    generatePomFileForKotlinMultiplatformPublication {
        dependsOn(project.tasks.named("generatePomFileForJvmPublication"))
    }

    val transformClasses by registering(TransformIntrinsicsTask::class) {
        inputDirectory = named<KotlinCompile>("compileKotlinJvm").flatMap { it.destinationDirectory }
        outputDirectory = layout.buildDirectory.dir("transformed-classes")
    }

    named<Jar>("jvmJar") {
        from(transformClasses.flatMap { it.outputDirectory })

        eachFile {
            val tree = project.tasks.named<KotlinCompile>("compileKotlinJvm").flatMap { it.destinationDirectory }.get().asFileTree
            if (tree.contains(file)) {
                exclude()
            }
        }
    }

}

publishing {
    publications {
        withType<MavenPublication>().configureEach {
            val emptyJavadocJar = tasks.register<Jar>("${name}JavadocJar") {
                archiveBaseName = "${archiveBaseName.get()}-${name}"
                archiveClassifier = "javadoc"
            }

            artifact(emptyJavadocJar)
        }

        // See https://github.com/GW2ToolBelt/GW2ChatLinks/issues/18
        named<MavenPublication>("kotlinMultiplatform") {
            val jvmPublication = publications.getByName<MavenPublication>("jvm")

            lateinit var jvmXml: XmlProvider
            jvmPublication.pom?.withXml { jvmXml = this }

            pom.withXml {
                val xmlProvider = this
                val root = xmlProvider.asNode()
                // Remove the original content and add the content from the platform POM:
                root.children().toList().forEach { root.remove(it as Node) }
                jvmXml.asNode().children().forEach { root.append(it as Node) }

                // Adjust the self artifact ID, as it should match the root module's coordinates:
                ((root.get("artifactId") as NodeList).first() as Node).setValue(artifactId)

                // Set packaging to POM to indicate that there's no artifact:
                root.appendNode("packaging", "pom")

                // Add a single dependency on the platform module:
                val dependencies = root.appendNode("dependencies", NodeList())
                val singleDependency = dependencies.appendNode("dependency")
                singleDependency.appendNode("groupId", jvmPublication.groupId)
                singleDependency.appendNode("artifactId", jvmPublication.artifactId)
                singleDependency.appendNode("version", jvmPublication.version)
                singleDependency.appendNode("scope", "compile")
            }
        }
    }
}
