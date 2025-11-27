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
package com.osmerion.omittable

import kotlin.test.*

class OmittableKotlinTest {

    @Test
    fun testOf() {
        val omittable = Omittable.of("Hello, World!")
        assertIs<Omittable.Present<*>>(omittable)
        assertTrue(omittable.isPresent())
        assertFalse(omittable.isAbsent())
    }

    @Test
    fun testOf_Null() {
        val omittable = Omittable.of(null)
        assertIs<Omittable.Present<*>>(omittable)
        assertTrue(omittable.isPresent())
        assertFalse(omittable.isAbsent())
    }

    @Test
    fun testAbsent() {
        val omittable = Omittable.absent<Any>()
        assertIsNot<Omittable.Present<*>>(omittable)
        assertTrue(omittable.isAbsent())
        assertFalse(omittable.isPresent())
    }

    @Test
    fun testIfPresent_Absent() {
        val omittable = Omittable.absent<Any>()
        var called = false
        omittable.ifPresent { called = true }
        assertFalse(called)
    }

    @Test
    fun testIfPresent_Present() {
        val omittable = Omittable.of("Hello, World!")
        var called = false
        omittable.ifPresent { called = true }
        assertTrue(called)
    }

    @Test
    fun testIfPresent_Present_Null() {
        val omittable = Omittable.of(null)
        var called = false
        omittable.ifPresent { called = true }
        assertTrue(called)
    }

    @Test
    fun testIfPresentOrElse_Absent() {
        val omittable = Omittable.absent<Any>()
        var presentCalled = false
        var absentCalled = false
        omittable.ifPresentOrElse(
            { presentCalled = true },
            { absentCalled = true }
        )
        assertFalse(presentCalled)
        assertTrue(absentCalled)
    }

    @Test
    fun testIfPresentOrElse_Present() {
        val omittable = Omittable.of("Hello, World!")
        var presentCalled = false
        var absentCalled = false
        omittable.ifPresentOrElse(
            { presentCalled = true },
            { absentCalled = true }
        )
        assertTrue(presentCalled)
        assertFalse(absentCalled)
    }

    @Test
    fun testIfPresentOrElse_Present_Null() {
        val omittable = Omittable.of(null)
        var presentCalled = false
        var absentCalled = false
        omittable.ifPresentOrElse(
            { presentCalled = true },
            { absentCalled = true }
        )
        assertTrue(presentCalled)
        assertFalse(absentCalled)
    }

    @Test
    fun testFilter_Absent() {
        val omittable = Omittable.absent<Any>()
        val filtered = omittable.filter { it is String }
        assertEquals(Omittable.absent(), filtered)
    }

    @Test
    fun testFilter_Present_Matches() {
        val omittable = Omittable.of("Hello, World!")
        val filtered = omittable.filter { it == "Hello, World!" }
        assertEquals(omittable, filtered)
    }

    @Test
    fun testFilter_Present_DoesNotMatch() {
        val omittable = Omittable.of("Hello, World!")
        val filtered = omittable.filter { it != "Hello, World!" }
        assertEquals(Omittable.absent(), filtered)
    }

    @Test
    fun testFilter_Present_Null_Matches() {
        val omittable = Omittable.of(null)
        val filtered = omittable.filter { true }
        assertEquals(omittable, filtered)
    }

    @Test
    fun testFilter_Present_Null_DoesNotMatch() {
        val omittable = Omittable.of(null)
        val filtered = omittable.filter { false }
        assertEquals(Omittable.absent(), filtered)
    }

    @Test
    fun testMap_Absent() {
        val omittable = Omittable.absent<Any>()
        val mapped = omittable.map { it.toString() }
        assertEquals(Omittable.absent(), mapped)
    }

    @Test
    fun testMap_Present() {
        val omittable = Omittable.of("Hello, World!")
        val mapped = omittable.map { it.length }
        assertEquals(Omittable.of(13), mapped)
    }

    @Test
    fun testMap_Present_Null() {
        val omittable = Omittable.of(null)
        val mapped = omittable.map { it?.toString()?.length ?: 0 }
        assertEquals(Omittable.of(0), mapped)
    }

    @Test
    fun testFlatMap_Absent() {
        val omittable = Omittable.absent<Any>()
        val flatMapped = omittable.flatMap { Omittable.of(it.toString()) }
        assertEquals(Omittable.absent(), flatMapped)
    }

    @Test
    fun testFlatMap_Present() {
        val omittable = Omittable.of("Hello, World!")
        val flatMapped = omittable.flatMap { Omittable.of(it.length) }
        assertEquals(Omittable.of(13), flatMapped)
    }

    @Test
    fun testFlatMap_Present_Null() {
        val omittable = Omittable.of(null)
        val flatMapped = omittable.flatMap { Omittable.of(it?.toString()?.length ?: 0) }
        assertEquals(Omittable.of(0), flatMapped)
    }

    @Test
    fun testOr_Absent() {
        val omittable = Omittable.absent<String>()
        val result = omittable.or { Omittable.of("Alternative") }
        assertEquals(Omittable.of("Alternative"), result)
    }

    @Test
    fun testOr_Present_Absent() {
        val omittable = Omittable.of("Hello, World!")
        val alternative = Omittable.absent<String>()
        val result = omittable.or { alternative }
        assertEquals(omittable, result)
    }

    @Test
    fun testOr_Present_Present() {
        val omittable = Omittable.of("Hello, World!")
        val alternative = Omittable.of("Alternative")
        val result = omittable.or { alternative }
        assertEquals(omittable, result)
    }

    @Test
    fun testOr_Present_Null() {
        val omittable = Omittable.of<String?>(null)
        val result = omittable.or { Omittable.of("Alternative") }
        assertEquals(omittable, result)
    }

    @Test
    fun testOrElse_Present() {
        val omittable = Omittable.of("Hello, World!")
        val result = omittable.orElse("Alternative")
        assertEquals("Hello, World!", result)
    }

    @Test
    fun testOrElse_Present_Null() {
        val omittable = Omittable.of<String?>(null)
        val result = omittable.orElse("Alternative")
        assertNull(result)
    }

    @Test
    fun testOrElse_Absent() {
        val omittable = Omittable.absent<String>()
        val result = omittable.orElse("Alternative")
        assertEquals("Alternative", result)
    }

    @Test
    fun testOrElse_Absent_Null() {
        val omittable = Omittable.absent<String?>()
        val result = omittable.orElse(null)
        assertNull(result)
    }

    @Test
    fun testOrElseGet_Present() {
        val omittable = Omittable.of("Hello, World!")
        val result = omittable.orElseGet { "Alternative" }
        assertEquals("Hello, World!", result)
    }

    @Test
    fun testOrElseGet_Present_Null() {
        val omittable = Omittable.of<String?>(null)
        val result = omittable.orElseGet { "Alternative" }
        assertNull(result)
    }

    @Test
    fun testOrElseGet_Absent() {
        val omittable = Omittable.absent<String>()
        val result = omittable.orElseGet { "Alternative" }
        assertEquals("Alternative", result)
    }

    @Test
    fun testOrElseGet_Absent_Null() {
        val omittable = Omittable.absent<String?>()
        val result = omittable.orElseGet { null }
        assertNull(result)
    }

    @Test
    fun testOrElseThrow_Present() {
        val omittable = Omittable.of("Hello, World!")
        val result = omittable.orElseThrow()
        assertEquals("Hello, World!", result)
    }

    @Test
    fun testOrElseThrow_Present_Null() {
        val omittable = Omittable.of<String?>(null)
        val result = omittable.orElseThrow()
        assertNull(result)
    }

    @Test
    fun testOrElseThrow_Absent() {
        val omittable = Omittable.absent<String>()
        assertFailsWith<NoSuchElementException> { omittable.orElseThrow() }
    }

    @Test
    fun testOrElseThrowEx_Present() {
        val omittable = Omittable.of("Hello, World!")
        val result = omittable.orElseThrow(::IllegalStateException)
        assertEquals("Hello, World!", result)
    }

    @Test
    fun testOrElseThrowEx_Present_Null() {
        val omittable = Omittable.of<String?>(null)
        val result = omittable.orElseThrow(::IllegalStateException)
        assertNull(result)
    }

    @Test
    fun testOrElseThrowEx_Absent() {
        val omittable = Omittable.absent<String>()
        assertFailsWith<IllegalStateException> { omittable.orElseThrow(::IllegalStateException) }
    }

    @Test
    fun testEquals_Absent() {
        assertEquals(Omittable.absent<Any>(), Omittable.absent())
    }

    @Test
    fun testEquals_Present() {
        val omittable1 = Omittable.of("Hello, World!")
        val omittable2 = Omittable.of("Hello, World!")
        assertEquals(omittable1, omittable2)
    }

    @Test
    fun testEquals_Present_Null() {
        val omittable1 = Omittable.of(null)
        val omittable2 = Omittable.of(null)
        assertEquals(omittable1, omittable2)
    }

    @Test
    fun testHashCode_Absent() {
        assertEquals(0, Omittable.absent<Any>().hashCode())
    }

    @Test
    fun testHashCode_Present() {
        val omittable = Omittable.of("Hello, World!")
        assertEquals("Hello, World!".hashCode(), omittable.hashCode())
    }

    @Test
    fun testHashCode_Present_Null() {
        val omittable = Omittable.of(null)
        assertEquals(0, omittable.hashCode())
    }

    @Test
    fun testToString_Absent() {
        assertEquals("Omittable.absent", Omittable.absent<Any>().toString())
    }

    @Test
    fun testToString_Present() {
        val omittable = Omittable.of("Hello, World!")
        assertEquals("Omittable[Hello, World!]", omittable.toString())
    }

    @Test
    fun testToString_Present_Null() {
        val omittable = Omittable.of(null)
        assertEquals("Omittable[null]", omittable.toString())
    }

}
