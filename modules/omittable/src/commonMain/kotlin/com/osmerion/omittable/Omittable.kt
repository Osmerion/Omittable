package com.osmerion.omittable

import com.osmerion.omittable.internal.kotlinx.serialization.OmittableSerializer
import kotlinx.serialization.Serializable

/**
 * A functional interface that accepts a single input argument and returns no result.
 *
 * @param T the type of the input to the operation
 *
 * @since   0.1.0
 */
public expect fun interface PlatformConsumer<T> {

    /**
     * Performs this operation on the given argument.
     *
     * @param t the input argument
     *
     * @since   0.1.0
     */
    public fun accept(t: T)

}

/**
 * A functional interface that accepts a single input argument and produces a result.
 *
 * @param T the type of the input to the function
 * @param R the type of the result of the function
 *
 * @since   0.1.0
 */
public expect fun interface PlatformFunction<T, R> {

    /**
     * Applies this function to the given argument.
     *
     * @param t the input argument
     *
     * @return the function result
     *
     * @since   0.1.0
     */
    public fun apply(t: T): R

}

/**
 * A functional interface that represents a runnable action.
 *
 * @since   0.1.0
 */
public expect fun interface PlatformRunnable {

    /**
     * Performs this runnable action.
     *
     * @since   0.1.0
     */
    public fun run()

}

/**
 * A functional interface that supplies a value.
 *
 * @param T the type of the supplied value
 *
 * @since   0.1.0
 */
public expect fun interface PlatformSupplier<T> {

    /**
     * Returns a result.
     *
     * @return a result
     *
     * @since   0.1.0
     */
    public fun get(): T

}

/**
 * A container object which may or may not contain a value.
 *
 * @since   0.1.0
 *
 * @author  Leon Linhart
 */
@Serializable(with = OmittableSerializer::class)
public expect sealed interface Omittable<T> {

    /**
     * A companion object providing factory methods for creating [Omittable] instances.
     *
     * @since   0.1.0
     */
    public companion object {

        /**
         * Returns an [Omittable] instance which represents the absence of a value.
         *
         * @param T the type of the absent value
         *
         * @return  an omittable that does not contain a value
         *
         * @since   0.1.0
         */
        public fun <T> absent(): Omittable<T>

        /**
         * Returns an [Omittable] instance containing the specified value.
         *
         * @param T     the type of the value
         * @param value the value to be contained in the [Omittable] instance
         *
         * @return  an omittable containing the specified value
         *
         * @since   0.1.0
         */
        public fun <T> of(value: T): Omittable<T>

    }

    /**
     * Returns `true` if no value is present, or `false` otherwise.
     *
     * @return  `true` if no value is present, or `false` otherwise
     *
     * @since   0.1.0
     */
    public fun isAbsent(): Boolean

    /**
     * Returns `true` if a value is present, or `false` otherwise.
     *
     * @return  `true` if a value is present, or `false` otherwise
     *
     * @since   0.1.0
     */
    public fun isPresent(): Boolean

    /**
     * If a value is present, performs the given action with the value, otherwise does nothing.
     *
     * @param action    the action to be performed, if a value is present
     *
     * @since   0.1.0
     */
    public fun ifPresent(action: PlatformConsumer<T>)

    /**
     * If a value is present, performs the given action with the value, otherwise performs the given absent action.
     *
     * @param action        the action to be performed, if a value is present
     * @param absentAction  the action to be performed, if no value is present
     *
     * @since   0.1.0
     */
    public fun ifPresentOrElse(action: PlatformConsumer<T>, absentAction: PlatformRunnable)

    /**
     * If a value is present, returns an [Omittable] containing that value if it matches the given predicate, otherwise
     * returns an absent [Omittable].
     *
     * @param predicate the predicate to be applied to the value, if present
     *
     * @return  an `Omittable` describing the value of this `Omittable`, if a value is present and the value matches the
     *          given predicate, otherwise an absent `Omittable`
     *
     * @since   0.1.0
     */
    public fun filter(predicate: PlatformFunction<T, Boolean>): Omittable<T>

    /**
     * If a value is present, returns an [Omittable] containing the result of applying the given mapping function
     * to the value, otherwise returns an absent [Omittable].
     *
     * @param U         the type of the value contained in the resulting [Omittable]
     * @param mapper    the mapping function to be applied to the value if present
     *
     * @return  an `Omittable` describing the result of applying a mapping function to the value of this `Omittable`, if
     *          a value is present, otherwise an absent `Omittable`
     *
     * @since   0.1.0
     */
    public fun <U> map(mapper: PlatformFunction<T, U>): Omittable<U>

    /**
     * If a value is present, returns an [Omittable] containing the result of applying the given mapping function to the
     * value, otherwise returns an absent [Omittable].
     *
     * @param U         the type of the value contained in the resulting [Omittable]
     * @param mapper    the mapping function to be applied to the value if present
     *
     * @return  the result of applying an `Omittable`-bearing mapping function to the value of this `Omittable`, if a
     *          value is present, otherwise an empty `Omittable`
     *
     * @since   0.1.0
     */
    public fun <U> flatMap(mapper: PlatformFunction<T, Omittable<U>>): Omittable<U>

    /**
     * If a value is present, returns an [Omittable] containing that value, otherwise returns an [Omittable] produced by
     * the supplying function.
     *
     * @param supplier  the supplying function to be invoked if no value is present
     *
     * @return  an `Omittable` describing the value of this `Omittable`, if a value is present, otherwise an `Omittable`
     *          produced by the supplying function
     *
     * @since   0.1.0
     */
    public fun or(supplier: PlatformSupplier<Omittable<T>>): Omittable<T>

    /**
     * If a value is present, returns the value, otherwise returns `other`.
     *
     * @param other the value to be returned if no value is present
     *
     * @return  the value, if present, otherwise `other`
     *
     * @since   0.2.0
     */
    public fun orElse(other: T): T

    /**
     * If a value is present, returns the value, otherwise returns the result produced by the supplying function.
     *
     * @param supplier  the supplying function that produces a value to be returned
     *
     * @return  the value, if present, otherwise the result produced by the supplying function
     *
     * @since   0.2.0
     */
    public fun orElseGet(supplier: PlatformSupplier<T>): T

    /**
     * If a value is present, returns the value, otherwise throws a [NoSuchElementException].
     *
     * @return  the value described by this omittable
     *
     * @since   0.2.0
     */
    public fun orElseThrow(): T

    /**
     * If a value is present, returns the value, otherwise throws an exception produced by the exception supplying
     * function.
     *
     * @return  the value described by this omittable
     *
     * @since   0.2.0
     */
    public fun orElseThrow(executionSupplier: PlatformSupplier<Throwable>): T

    /**
     * A sentinel [Omittable] instance which represents the absence of a value.
     *
     * @since   0.1.0
     */
    public object Absent : Omittable<Any?> {

        override fun isAbsent(): Boolean
        override fun isPresent(): Boolean

        override fun ifPresent(action: PlatformConsumer<Any?>)
        override fun ifPresentOrElse(action: PlatformConsumer<Any?>, absentAction: PlatformRunnable)

        override fun filter(predicate: PlatformFunction<Any?, Boolean>): Omittable<Any?>
        override fun <U> map(mapper: PlatformFunction<Any?, U>): Omittable<U>
        override fun <U> flatMap(mapper: PlatformFunction<Any?, Omittable<U>>): Omittable<U>
        override fun or(supplier: PlatformSupplier<Omittable<Any?>>): Omittable<Any?>
        override fun orElse(other: Any?): Any?
        override fun orElseGet(supplier: PlatformSupplier<Any?>): Any?
        override fun orElseThrow(): Any?
        override fun orElseThrow(executionSupplier: PlatformSupplier<Throwable>): Any?

    }

    /**
     * An [Omittable] instance which contains a value.
     *
     * @since   0.1.0
     */
    public class Present<T> : Omittable<T> {

        /**
         * Returns the value contained in this [Omittable] instance.
         *
         * @since   0.1.0
         */
        public val value: T

        override fun isAbsent(): Boolean
        override fun isPresent(): Boolean

        override fun ifPresent(action: PlatformConsumer<T>)
        override fun ifPresentOrElse(action: PlatformConsumer<T>, absentAction: PlatformRunnable)

        override fun filter(predicate: PlatformFunction<T, Boolean>): Omittable<T>
        override fun <U> map(mapper: PlatformFunction<T, U>): Omittable<U>
        override fun <U> flatMap(mapper: PlatformFunction<T, Omittable<U>>): Omittable<U>
        override fun or(supplier: PlatformSupplier<Omittable<T>>): Omittable<T>
        override fun orElse(other: T): T
        override fun orElseGet(supplier: PlatformSupplier<T>): T
        override fun orElseThrow(): T
        override fun orElseThrow(executionSupplier: PlatformSupplier<Throwable>): T

    }

}
