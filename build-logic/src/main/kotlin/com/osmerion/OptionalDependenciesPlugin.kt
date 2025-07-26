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
package com.osmerion

import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.api.artifacts.Configuration
import org.gradle.api.plugins.JavaPlugin
import org.gradle.api.plugins.JavaPluginExtension
import org.gradle.kotlin.dsl.getByType
import org.gradle.kotlin.dsl.withType

class OptionalDependenciesPlugin : Plugin<Project> {

    companion object {
        const val OPTIONAL_CONFIGURATION_NAME: String = "optional"
    }

    override fun apply(project: Project) {
        val optional: Configuration = project.configurations.create(OPTIONAL_CONFIGURATION_NAME)
        optional.isCanBeConsumed = false
        optional.isCanBeResolved = false

        project.plugins.withType<JavaPlugin> {
            val sourceSets = project.extensions
                .getByType<JavaPluginExtension>()
                .sourceSets

            sourceSets.all {
                project.configurations
                    .getByName(compileClasspathConfigurationName)
                    .extendsFrom(optional)

                project.configurations
                    .getByName(runtimeClasspathConfigurationName)
                    .extendsFrom(optional)
            }
        }
    }

}
