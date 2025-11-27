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

import com.osmerion.omittable.internal.kotlinx.serialization.OmittableSerializer
import kotlinx.serialization.Serializable
import java.util.function.Consumer
import java.util.function.Function
import java.util.function.Supplier
import java.util.stream.Stream
import kotlin.NoSuchElementException

public actual typealias PlatformConsumer<T> = Consumer<T>
public actual typealias PlatformFunction<T, U> = Function<T, U>
public actual typealias PlatformRunnable = Runnable
public actual typealias PlatformSupplier<T> = Supplier<T>

@Serializable(with = OmittableSerializer::class)
public actual sealed interface Omittable<T> {

    public actual companion object {

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        public actual fun <T> absent(): Omittable<T> = Absent as Omittable<T>

        @JvmStatic
        public actual fun <T> of(value: T): Omittable<T> = Present(value)

    }

    public actual fun isAbsent(): Boolean
    public actual fun isPresent(): Boolean

    public actual fun ifPresent(action: PlatformConsumer<T>)
    public actual fun ifPresentOrElse(action: PlatformConsumer<T>, absentAction: PlatformRunnable)

    public actual fun filter(predicate: PlatformFunction<T, Boolean>): Omittable<T>
    public actual fun <U> map(mapper: PlatformFunction<T, U>): Omittable<U>
    public actual fun <U> flatMap(mapper: PlatformFunction<T, Omittable<U>>): Omittable<U>
    public actual fun or(supplier: PlatformSupplier<Omittable<T>>): Omittable<T>
    public actual fun orElse(other: T): T
    public actual fun orElseGet(supplier: PlatformSupplier<T>): T
    public actual fun orElseThrow(): T
    public actual fun orElseThrow(executionSupplier: PlatformSupplier<Throwable>): T

    /**
     * If a value is present, returns a [Stream] containing only that value, otherwise returns an empty [Stream].
     *
     * @since   0.1.0
     */
    public fun stream(): Stream<T>

    public actual object Absent : Omittable<Any?> {

        actual override fun isAbsent(): Boolean = true
        actual override fun isPresent(): Boolean = false

        actual override fun ifPresent(action: PlatformConsumer<Any?>) {
            // No-op
        }

        actual override fun ifPresentOrElse(action: PlatformConsumer<Any?>, absentAction: PlatformRunnable) {
            absentAction.run()
        }

        actual override fun filter(predicate: PlatformFunction<Any?, Boolean>): Omittable<Any?> = absent()
        actual override fun <U> map(mapper: PlatformFunction<Any?, U>): Omittable<U> = absent()
        actual override fun <U> flatMap(mapper: PlatformFunction<Any?, Omittable<U>>): Omittable<U> = absent()

        actual override fun or(supplier: PlatformSupplier<Omittable<Any?>>): Omittable<Any?> = supplier.get()
        actual override fun orElse(other: Any?): Any? = other
        actual override fun orElseGet(supplier: PlatformSupplier<Any?>): Any? = supplier.get()
        actual override fun orElseThrow(): Any? = throw NoSuchElementException()
        actual override fun orElseThrow(executionSupplier: PlatformSupplier<Throwable>): Any? = throw executionSupplier.get()

        override fun stream(): Stream<Any?> =
            Stream.empty()

        override fun equals(other: Any?): Boolean = this === other
        override fun hashCode(): Int = 0
        override fun toString(): String = "Omittable.absent"

    }

    @JvmRecord
    public actual data class Present<T> public constructor(public actual val value: T) : Omittable<T> {

        actual override fun isAbsent(): Boolean = false
        actual override fun isPresent(): Boolean = true

        actual override fun ifPresent(action: PlatformConsumer<T>) {
            action.accept(value)
        }

        actual override fun ifPresentOrElse(action: PlatformConsumer<T>, absentAction: PlatformRunnable) {
            action.accept(value)
        }

        actual override fun filter(predicate: PlatformFunction<T, Boolean>): Omittable<T> =
            if (predicate.apply(value)) this else absent()

        actual override fun <U> map(mapper: PlatformFunction<T, U>): Omittable<U> =
            of(mapper.apply(value))

        actual override fun <U> flatMap(mapper: PlatformFunction<T, Omittable<U>>): Omittable<U> {
            val result = mapper.apply(value)
            return requireNotNull(result)
        }

        actual override fun or(supplier: PlatformSupplier<Omittable<T>>): Omittable<T> = this
        actual override fun orElse(other: T): T = value
        actual override fun orElseGet(supplier: PlatformSupplier<T>): T = value
        actual override fun orElseThrow(): T = value
        actual override fun orElseThrow(executionSupplier: PlatformSupplier<Throwable>): T = value

        override fun stream(): Stream<T> =
            Stream.of(value)

        override fun equals(other: Any?): Boolean = when {
            this === other -> true
            other !is Present<*> -> false
            else -> value == other.value
        }

        override fun hashCode(): Int = value.hashCode()
        override fun toString(): String = "Omittable[$value]"

    }

}
