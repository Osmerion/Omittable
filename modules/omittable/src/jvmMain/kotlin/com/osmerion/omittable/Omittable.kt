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
public actual sealed class Omittable<T> {

    public actual companion object {

        @JvmStatic
        @Suppress("UNCHECKED_CAST")
        public actual fun <T> absent(): Omittable<T> = Absent as Omittable<T>

        @JvmStatic
        public actual fun <T> of(value: T): Omittable<T> = Present(value)

    }

    public actual abstract fun getOrThrow(): T

    public actual abstract fun isAbsent(): Boolean
    public actual abstract fun isPresent(): Boolean

    public actual abstract fun ifPresent(action: PlatformConsumer<T>)
    public actual abstract fun ifPresentOrElse(action: PlatformConsumer<T>, absentAction: PlatformRunnable)

    public actual abstract fun filter(predicate: PlatformFunction<T, Boolean>): Omittable<T>
    public actual abstract fun <U> map(mapper: PlatformFunction<T, U>): Omittable<U>
    public actual abstract fun <U> flatMap(mapper: PlatformFunction<T, Omittable<U>>): Omittable<U>
    public actual abstract fun or(supplier: PlatformSupplier<Omittable<T>>): Omittable<T>

    /**
     * If a value is present, returns a [Stream] containing only that value, otherwise returns an empty [Stream].
     *
     * @since   0.1.0
     */
    public abstract fun stream(): Stream<T>

    internal actual object Absent : Omittable<Any>() {

        actual override fun getOrThrow(): Any = throw NoSuchElementException("No value present")

        actual override fun isAbsent(): Boolean = true
        actual override fun isPresent(): Boolean = false

        actual override fun ifPresent(action: PlatformConsumer<Any>) {
            // No-op
        }

        actual override fun ifPresentOrElse(action: PlatformConsumer<Any>, absentAction: PlatformRunnable) {
            absentAction.run()
        }

        actual override fun filter(predicate: PlatformFunction<Any, Boolean>): Omittable<Any> = absent()
        actual override fun <U> map(mapper: PlatformFunction<Any, U>): Omittable<U> = absent()
        actual override fun <U> flatMap(mapper: PlatformFunction<Any, Omittable<U>>): Omittable<U> = absent()

        actual override fun or(supplier: PlatformSupplier<Omittable<Any>>): Omittable<Any> =
            supplier.get()

        override fun stream(): Stream<Any> =
            Stream.empty()

        override fun equals(other: Any?): Boolean = this === other
        override fun hashCode(): Int = 0
        override fun toString(): String = "Omittable.absent"

    }

    public actual class Present<T> internal constructor(private val value: T) : Omittable<T>() {

        public actual override fun getOrThrow(): T = value

        public actual override fun isAbsent(): Boolean = false
        public actual override fun isPresent(): Boolean = true

        public actual override fun ifPresent(action: PlatformConsumer<T>) {
            action.accept(value)
        }

        public actual override fun ifPresentOrElse(action: PlatformConsumer<T>, absentAction: PlatformRunnable) {
            action.accept(value)
        }

        public actual override fun filter(predicate: PlatformFunction<T, Boolean>): Omittable<T> =
            if (predicate.apply(value)) this else absent()

        public actual override fun <U> map(mapper: PlatformFunction<T, U>): Omittable<U> =
            of(mapper.apply(value))

        public actual override fun <U> flatMap(mapper: PlatformFunction<T, Omittable<U>>): Omittable<U> {
            val result = mapper.apply(value)
            return requireNotNull(result)
        }

        public actual override fun or(supplier: PlatformSupplier<Omittable<T>>): Omittable<T> =
            this

        public override fun stream(): Stream<T> =
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
