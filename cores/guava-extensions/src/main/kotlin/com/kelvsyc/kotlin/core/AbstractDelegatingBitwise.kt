package com.kelvsyc.kotlin.core

/**
 * Implementation of [Bitwise] for types where bitwise operations can be performed by converting instances of that type
 * to a different type for which bitwise operations are well-defined.
 *
 * Because of this, the delegated type [U] is immutable.
 *
 * @param T the type for which bitwise operations are defined
 * @param U the delegated type for which bitwise operations are performed
 */
abstract class AbstractDelegatingBitwise<T : Any, U : Any> : AbstractConverterBasedBitwise<T, U>() {
    abstract val inner: Bitwise<U>

    override fun doAnd(lhs: U, rhs: U): U = inner.and(lhs, rhs)
    override fun doOr(lhs: U, rhs: U): U = inner.or(lhs, rhs)
    override fun doXor(lhs: U, rhs: U): U = inner.xor(lhs, rhs)
    override fun doInv(value: U): U = inner.inv(value)

    override fun doLeftShift(value: U, bitCount: Int): U = inner.leftShift(value, bitCount)
    override fun doArithmeticRightShift(value: U, bitCount: Int): U = inner.arithmeticRightShift(value, bitCount)
    override fun doLogicalRightShift(value: U, bitCount: Int): U = inner.logicalRightShift(value, bitCount)

    override fun doRotateLeft(value: U, bitCount: Int): U = inner.rotateLeft(value, bitCount)
    override fun doRotateRight(value: U, bitCount: Int): U = inner.rotateRight(value, bitCount)
}
