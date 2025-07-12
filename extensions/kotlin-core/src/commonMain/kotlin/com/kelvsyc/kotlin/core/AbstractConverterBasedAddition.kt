package com.kelvsyc.kotlin.core

/**
 * Implementation of [Addition] for which bitwise operations can be done by converting the type to that of another type
 * for which [Addition] operations are already defined.
 *
 * The default implementation will convert its operands using the specified [Converter] to the delegated type, then
 * convert the result back to the original type using the [reverse Converter][Converter.reverse]. This can be overridden
 * in subclasses if necessary.
 *
 * @param T The type supporting bitwise operations.
 * @param U The delegated type.
 */
abstract class AbstractConverterBasedAddition<T, U> : Addition<T> {
    abstract val inner: Addition<U>
    abstract val converter: Converter<T, U>

    override fun add(lhs: T, rhs: T): T = converter.reverse(inner.add(converter(lhs), converter(rhs)))
    override fun subtract(lhs: T, rhs: T): T = converter.reverse(inner.subtract(converter(lhs), converter(rhs)))
}
