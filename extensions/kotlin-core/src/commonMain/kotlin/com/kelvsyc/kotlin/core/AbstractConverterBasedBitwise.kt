package com.kelvsyc.kotlin.core

/**
 * Implementation of [Bitwise] for which bitwise operations can be done by converting the type to that of another type
 * for which [Bitwise] operations are already defined.
 *
 * The default implementation will convert its operands using the specified [Converter] to the delegated type, then
 * convert the result back to the original type using the [reverse Converter][Converter.reverse]. This can be overridden
 * in subclasses if necessary.
 *
 * @param T The type supporting bitwise operations.
 * @param U The delegated type.
 */
abstract class AbstractConverterBasedBitwise<T, U> : Bitwise<T> {
    abstract val inner: Bitwise<U>
    abstract val converter: Converter<T, U>

    override fun and(lhs: T, rhs: T): T = converter.reverse(inner.and(converter(lhs), converter(rhs)))
    override fun or(lhs: T, rhs: T): T = converter.reverse(inner.or(converter(lhs), converter(rhs)))
    override fun xor(lhs: T, rhs: T): T = converter.reverse(inner.xor(converter(lhs), converter(rhs)))
    override fun inv(value: T): T = converter.reverse(inner.inv(converter(value)))
}
