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
pluginManagement {
    plugins {
        id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
    }

    includeBuild("build-logic")
}

plugins {
    id("org.gradle.toolchains.foojay-resolver-convention")
}

rootProject.name = "Omittable"

dependencyResolutionManagement {
    // https://youtrack.jetbrains.com/issue/KT-68533
//    repositoriesMode = RepositoriesMode.FAIL_ON_PROJECT_REPOS

    repositories {
        mavenCentral()
    }

    versionCatalogs {
        register("buildDeps") {
            from(files("./gradle/build.versions.toml"))
        }
    }
}

enableFeaturePreview("STABLE_CONFIGURATION_CACHE")

include(":omittable")
project(":omittable").projectDir = file("modules/omittable")

include(":omittable-bom")
project(":omittable-bom").projectDir = file("modules/omittable-bom")

include(":omittable-jackson")
project(":omittable-jackson").projectDir = file("modules/omittable-jackson")

include(":omittable-spring-boot-webflux")
project(":omittable-spring-boot-webflux").projectDir = file("modules/omittable-spring-boot-webflux")

include(":omittable-spring-boot-webmvc")
project(":omittable-spring-boot-webmvc").projectDir = file("modules/omittable-spring-boot-webmvc")

include(":omittable-spring-webflux")
project(":omittable-spring-webflux").projectDir = file("modules/omittable-spring-webflux")

include(":omittable-spring-webmvc")
project(":omittable-spring-webmvc").projectDir = file("modules/omittable-spring-webmvc")

include(":omittable-swagger-core")
project(":omittable-swagger-core").projectDir = file("modules/omittable-swagger-core")
