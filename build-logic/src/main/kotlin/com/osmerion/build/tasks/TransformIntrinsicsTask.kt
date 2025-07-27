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
package com.osmerion.build.tasks

import org.gradle.api.DefaultTask
import org.gradle.api.file.DirectoryProperty
import org.gradle.api.tasks.CacheableTask
import org.gradle.api.tasks.InputDirectory
import org.gradle.api.tasks.OutputDirectory
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import java.lang.classfile.ClassFile
import java.lang.classfile.ClassTransform
import java.lang.classfile.CodeTransform
import java.lang.classfile.instruction.InvokeInstruction
import java.lang.constant.ClassDesc
import java.lang.constant.MethodTypeDesc
import java.util.jar.JarEntry
import java.util.jar.JarInputStream
import java.util.jar.JarOutputStream

@CacheableTask
abstract class TransformIntrinsicsTask : DefaultTask() {

    @get:InputDirectory
    @get:PathSensitive(PathSensitivity.RELATIVE)
    abstract val inputDirectory: DirectoryProperty

    @get:OutputDirectory
    abstract val outputDirectory: DirectoryProperty

    @TaskAction
    protected fun transform() {
        outputDirectory.get().asFile.deleteRecursively()

        inputDirectory.asFileTree.visit {
            val targetFile = outputDirectory.get().file(path).asFile

            if (isDirectory) {
                targetFile.mkdirs()
            } else if (name.endsWith(".class")) {
                targetFile.writeBytes(transform(this.file.readBytes()))
            } else {
                file.copyTo(targetFile)
            }
        }
    }

    // See kotlin/jvm/internal/Intrinsics

    private companion object {
        const val KOTLIN_INTRINSICS: String = "kotlin/jvm/internal/Intrinsics"
        val JAVA_OBJECTS: ClassDesc = ClassDesc.of("java.util.Objects")
    }

    private fun transform(bytes: ByteArray): ByteArray {
        val cls = ClassFile.of()
        val model = cls.parse(bytes)

        val codeTransform = CodeTransform { builder, element ->
            if (element is InvokeInstruction && element.owner().name().stringValue() == KOTLIN_INTRINSICS) {
                when (element.name().stringValue()) {
                    "areEqual" -> {
                        builder.invokestatic(
                            JAVA_OBJECTS, "equals",
                            MethodTypeDesc.of(
                                ClassDesc.ofDescriptor("Z"),
                                ClassDesc.ofDescriptor("Ljava/lang/Object;"),
                                ClassDesc.ofDescriptor("Ljava/lang/Object;")
                            )
                        )
                    }
                    "checkNotNull", "checkNotNullExpressionValue", "checkNotNullParameter" -> {
                        builder.invokestatic(
                            JAVA_OBJECTS, "requireNonNull",
                            MethodTypeDesc.of(
                                ClassDesc.ofDescriptor("Ljava/lang/Object;"),
                                ClassDesc.ofDescriptor("Ljava/lang/Object;"),
                                ClassDesc.ofDescriptor("Ljava/lang/String;")
                            )
                        )
                        builder.pop();
                    }
                    else -> UnsupportedOperationException("No transformation defined for Intrinsics.${element.name().stringValue()}")
                }
            } else {
                builder.with(element)
            }

        }

        return cls.transformClass(model, ClassTransform.transformingMethodBodies(codeTransform))
    }

}
