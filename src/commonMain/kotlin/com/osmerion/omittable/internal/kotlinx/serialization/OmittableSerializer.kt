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
package com.osmerion.omittable.internal.kotlinx.serialization

import com.osmerion.omittable.Omittable
import kotlinx.serialization.KSerializer
import kotlinx.serialization.descriptors.SerialDescriptor
import kotlinx.serialization.encoding.Decoder
import kotlinx.serialization.encoding.Encoder

internal class OmittableSerializer<T>(
    private val serializer: KSerializer<T>
) : KSerializer<Omittable<T>> {

    override val descriptor: SerialDescriptor
        get() = serializer.descriptor

    override fun serialize(encoder: Encoder, value: Omittable<T>) {
        value.ifPresent { serializer.serialize(encoder, it) }
    }

    override fun deserialize(decoder: Decoder): Omittable<T> {
        val value = decoder.decodeSerializableValue(serializer)
        return Omittable.of(value)
    }

}
