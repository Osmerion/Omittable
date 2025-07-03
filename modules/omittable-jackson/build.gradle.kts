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
plugins {
    id("com.osmerion.java-base-conventions")
    id("com.osmerion.maven-publish-conventions")
    `java-library`
}

java {
    withSourcesJar()
    withJavadocJar()
}

publishing {
    publications.register<MavenPublication>("mavenJava") {
        from(components["java"])

        pom {
            description = "Jackson module for the Omittable library, providing support for serializing and deserializing Omittable types."
        }
    }
}

dependencies {
    api(project(":omittable", configuration = "archives"))
    api(libs.jackson.databind)

    testImplementation(project.dependencies.platform(buildDeps.junit.bom))
    testImplementation(buildDeps.assertj.core)
    testImplementation(buildDeps.junit.jupiter.api)
    testImplementation(buildDeps.kotlin.test.junit5)

    testRuntimeOnly(buildDeps.junit.jupiter.engine)
    testRuntimeOnly(buildDeps.junit.platform.launcher)
}
