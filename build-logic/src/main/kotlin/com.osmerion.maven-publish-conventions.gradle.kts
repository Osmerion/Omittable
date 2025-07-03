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
    `maven-publish`
    signing
}

publishing {
    publications.withType<MavenPublication>().configureEach {
        pom {
            name = project.name
            description = "A tiny Kotlin Multiplatform library that provides an Omittable type to be used in cases where an absence of a value is semantically different from a null value."
            url = "https://github.com/Osmerion/Omittable"

            licenses {
                license {
                    name = "Apache-2.0"
                    url = "https://github.com/Osmerion/Omittable/blob/master/LICENSE"
                    distribution = "repo"
                }
            }

            developers {
                developer {
                    id = "TheMrMilchmann"
                    name = "Leon Linhart"
                    email = "themrmilchmann@gmail.com"
                    url = "https://github.com/TheMrMilchmann"
                }
            }

            scm {
                connection = "scm:git:git://github.com/Osmerion/Omittable.git"
                developerConnection = "scm:git:git://github.com/Osmerion/Omittable.git"
                url = "https://github.com/Osmerion/Omittable.git"
            }
        }
    }
}

signing {
    // Only require signing when publishing to a non-local maven repository
    setRequired { gradle.taskGraph.allTasks.any { it is PublishToMavenRepository } }

    val signingKey: String? by project
    val signingPassword: String? by project
    useInMemoryPgpKeys(signingKey, signingPassword)

    sign(publishing.publications)
}
