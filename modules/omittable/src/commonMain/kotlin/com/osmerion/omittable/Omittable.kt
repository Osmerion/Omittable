package com.osmerion.omittable

import com.osmerion.omittable.internal.kotlinx.serialization.OmittableSerializer
import kotlinx.serialization.Serializable
import kotlin.jvm.JvmRecord

public expect fun interface PlatformConsumer<T> {
    public fun accept(t: T)
}

public expect fun interface PlatformFunction<T, R> {
    public fun apply(t: T): R
}

public expect fun interface PlatformRunnable {
    public fun run()
}

public expect fun interface PlatformSupplier<T> {
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

    public companion object {

        /**
         * Returns an [Omittable] instance which represents the absence of a value.
         *
         * @param T the type of the absent value
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
         * @since   0.1.0
         */
        public fun <T> of(value: T): Omittable<T>

    }

    /**
     * Returns the value contained in this [Omittable] instance, if present.
     *
     * @throws [NoSuchElementException] if no value is present
     *
     * @since   0.1.0
     */
    public fun getOrThrow(): T

    /**
     * Returns `true` if no value is present, or `false` otherwise.
     *
     * @since   0.1.0
     */
    public fun isAbsent(): Boolean

    /**
     * Returns `true` if a value is present, or `false` otherwise.
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
     * @since   0.1.0
     */
    public fun <U> flatMap(mapper: PlatformFunction<T, Omittable<U>>): Omittable<U>

    /**
     * If a value is present, returns an [Omittable] containing that value, otherwise returns an [Omittable] produced by
     * the supplying function.
     *
     * @param supplier  the supplying function to be invoked if no value is present
     * 
     * @since   0.1.0
     */
    public fun or(supplier: PlatformSupplier<Omittable<T>>): Omittable<T>

    public object Absent : Omittable<Any> {

        override fun getOrThrow(): Any

        override fun isAbsent(): Boolean
        override fun isPresent(): Boolean

        override fun ifPresent(action: PlatformConsumer<Any>)
        override fun ifPresentOrElse(action: PlatformConsumer<Any>, absentAction: PlatformRunnable)

        override fun filter(predicate: PlatformFunction<Any, Boolean>): Omittable<Any>
        override fun <U> map(mapper: PlatformFunction<Any, U>): Omittable<U>
        override fun <U> flatMap(mapper: PlatformFunction<Any, Omittable<U>>): Omittable<U>
        override fun or(supplier: PlatformSupplier<Omittable<Any>>): Omittable<Any>

    }

    /**
     * An [Omittable] instance which contains a value.
     *
     * @since   0.1.0
     */
    public class Present<T> : Omittable<T> {

        public val value: T

        override fun getOrThrow(): T

        public override fun isAbsent(): Boolean
        public override fun isPresent(): Boolean

        public override fun ifPresent(action: PlatformConsumer<T>)
        public override fun ifPresentOrElse(action: PlatformConsumer<T>, absentAction: PlatformRunnable)

        public override fun filter(predicate: PlatformFunction<T, Boolean>): Omittable<T>
        public override fun <U> map(mapper: PlatformFunction<T, U>): Omittable<U>
        public override fun <U> flatMap(mapper: PlatformFunction<T, Omittable<U>>): Omittable<U>
        public override fun or(supplier: PlatformSupplier<Omittable<T>>): Omittable<T>

    }

}
