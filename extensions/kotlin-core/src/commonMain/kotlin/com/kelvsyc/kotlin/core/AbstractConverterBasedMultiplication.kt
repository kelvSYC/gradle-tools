package com.kelvsyc.kotlin.core

/**
 * Implementation of [Multiplication] for which bitwise operations can be done by converting the type to that of another
 * type for which [Multiplication] operations are already defined.
 *
 * The default implementation will convert its operands using the specified [Converter] to the delegated type, then
 * convert the result back to the original type using the [reverse Converter][Converter.reverse]. This can be overridden
 * in subclasses if necessary.
 *
 * @param T The type supporting bitwise operations.
 * @param U The delegated type.
 */
abstract class AbstractConverterBasedMultiplication<T, U> : Multiplication<T> {
    abstract val inner: Multiplication<U>
    abstract val converter: Converter<T, U>

    override fun multiply(lhs: T, rhs: T): T = converter.reverse(inner.multiply(converter(lhs), converter(rhs)))
    override fun divide(lhs: T, rhs: T): T = converter.reverse(inner.divide(converter(lhs), converter(rhs)))
}
