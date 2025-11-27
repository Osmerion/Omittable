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
package com.osmerion.omittable.kotlinx.serialization

import com.osmerion.omittable.Omittable
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.Json
import kotlin.test.Test
import kotlin.test.assertContains
import kotlin.test.assertEquals

class OmittableSerializerTest {

    @Serializable
    data class TestDto(
        // Default values are crucial for kotlinx.serialization to omit absent fields
        val name: Omittable<String> = Omittable.absent(),
        val count: Omittable<Int> = Omittable.absent(),
        val nullableValue: Omittable<String?> = Omittable.absent()
    )

    // The Json instance is configured to not encode default values,
    // which is the default and desired behavior for omitting absent fields.
    private val json = Json {
        prettyPrint = true
    }

    @Test
    fun `should not serialize absent value`() {
        val dto = TestDto(name = Omittable.of("Test"), count = Omittable.of(123))
        val result = json.encodeToString(TestDto.serializer(), dto)

        assertEquals(
            """
            {
                "name": "Test",
                "count": 123
            }
            """.trimIndent(),
            result
        )

        assertEquals(dto, json.decodeFromString<TestDto>(result))
    }

    @Test
    fun `serializes null present value`() {
        val dto = TestDto(name = Omittable.of("Test"), count = Omittable.of(123), nullableValue = Omittable.of(null))
        val result = json.encodeToString(TestDto.serializer(), dto)

        assertEquals(
            """
            {
                "name": "Test",
                "count": 123,
                "nullableValue": null
            }
            """.trimIndent(),
            result
        )

        assertEquals(dto, json.decodeFromString<TestDto>(result))
    }

}
