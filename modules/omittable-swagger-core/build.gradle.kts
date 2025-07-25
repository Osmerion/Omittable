plugins {
    id("com.osmerion.java-base-conventions")
    id("com.osmerion.maven-publish-conventions")
    `java-library`
    `jvm-test-suite`
}

java {
    withSourcesJar()
    withJavadocJar()
}

testing {
    suites {
        named<JvmTestSuite>("test") {
            useJUnitJupiter()

            dependencies {
                implementation(project.dependencies.platform(buildDeps.junit.bom))
                implementation(buildDeps.assertj.core)
                implementation(buildDeps.junit.jupiter.api)

                runtimeOnly(buildDeps.junit.jupiter.engine)
                runtimeOnly(buildDeps.junit.platform.launcher)
            }
        }
    }
}

publishing {
    publications.register<MavenPublication>("mavenJava") {
        from(components["java"])

        pom {
            description = "Swagger support library with converters for Omittable types."
        }
    }
}

dependencies {
    api(project(":omittable-jackson"))
    api(libs.swagger.core.jakarta)
}
