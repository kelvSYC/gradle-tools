package com.kelvsyc.kotlin.core

/**
 * Implementation of [BitRotate] for which bit rotation operations can be done by converting the type to that of another
 * type for which [BitRotate] operations are already defined.
 *
 * The default implementation will convert its operands using the specified [Converter] to the delegated type, then
 * convert the result back to the original type using the [reverse Converter][Converter.reverse]. This can be overridden
 * in subclasses if necessary.
 *
 * @param T The type supporting bitwise operations.
 * @param U The delegated type.
 */
abstract class AbstractConverterBasedBitRotate<T, U> : BitRotate<T> {
    abstract val inner: BitRotate<U>
    abstract val converter: Converter<T, U>

    override fun rotateLeft(value: T, bitCount: Int): T =
        converter.reverse(inner.rotateLeft(converter(value), bitCount))
    override fun rotateRight(value: T, bitCount: Int): T =
        converter.reverse(inner.rotateRight(converter(value), bitCount))
}
