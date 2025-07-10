package com.kelvsyc.kotlin.core

/**
 * Implementation of [BitShift] for which bit shifting operations can be done by converting the type to that of another
 * type for which [BitShift] operations are already defined.
 *
 * The default implementation will convert its operands using the specified [Converter] to the delegated type, then
 * convert the result back to the original type using the [reverse Converter][Converter.reverse]. This can be overridden
 * in subclasses if necessary.
 *
 * @param T The type supporting bitwise operations.
 * @param U The delegated type.
 */
abstract class AbstractConverterBasedBitShift<T, U> : BitShift<T> {
    abstract val inner: BitShift<U>
    abstract val converter: Converter<T, U>

    override fun leftShift(value: T, bitCount: Int): T =
        converter.reverse(inner.leftShift(converter(value), bitCount))
    override fun rightShift(value: T, bitCount: Int): T =
        converter.reverse(inner.rightShift(converter(value), bitCount))
    override fun arithmeticRightShift(value: T, bitCount: Int): T =
        converter.reverse(inner.arithmeticRightShift(converter(value), bitCount))
}
