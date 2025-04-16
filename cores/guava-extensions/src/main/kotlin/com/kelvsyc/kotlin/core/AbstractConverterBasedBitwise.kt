package com.kelvsyc.kotlin.core

import com.google.common.base.Converter

/**
 * Implementation of [Bitwise] for types for which bitwise operations can be done by converting the type to a different
 * type for which bitwise operations are better defined.
 *
 * The delegated type, [U], may be a mutable type. It is generally recommended that it be the case, to facilitate
 * in-place operations on the type.
 *
 * @param T the type for which bitwise operations are defined
 * @param U the delegated type for which bitwise operations are performed
 */
abstract class AbstractConverterBasedBitwise<T : Any, U : Any> : Bitwise<T> {
    /**
     * [Converter] instance that converts objects of the specified type to the delegate type.
     */
    abstract val converter: Converter<T, U>

    protected abstract fun doAnd(lhs: U, rhs: U): U
    protected abstract fun doOr(lhs: U, rhs: U): U
    protected abstract fun doXor(lhs: U, rhs: U): U
    protected abstract fun doInv(value: U): U

    protected abstract fun doLeftShift(value: U, bitCount: Int): U
    protected abstract fun doArithmeticRightShift(value: U, bitCount: Int): U
    protected abstract fun doLogicalRightShift(value: U, bitCount: Int): U

    protected abstract fun doRotateLeft(value: U, bitCount: Int): U
    protected abstract fun doRotateRight(value: U, bitCount: Int): U

    private fun performBinaryOperation(lhs: T, rhs: T, op: (U, U) -> U): T {
        val l = converter.convert(lhs)!!
        val r = converter.convert(rhs)!!
        return converter.reverse().convert(op(l, r))!!
    }

    private fun performShiftOperation(value: T, bitCount: Int, op: (U, Int) -> U): T {
        val inner = converter.convert(value)!!
        return converter.reverse().convert(op(inner, bitCount))!!
    }

    final override fun and(lhs: T, rhs: T): T = performBinaryOperation(lhs, rhs, ::doAnd)
    final override fun or(lhs: T, rhs: T): T = performBinaryOperation(lhs, rhs, ::doOr)
    final override fun xor(lhs: T, rhs: T): T = performBinaryOperation(lhs, rhs, ::doXor)
    final override fun inv(value: T): T {
        val inner = converter.convert(value)!!
        return converter.reverse().convert(doInv(inner))!!
    }

    final override fun leftShift(value: T, bitCount: Int): T = performShiftOperation(value, bitCount, ::doLeftShift)
    final override fun arithmeticRightShift(value: T, bitCount: Int): T = performShiftOperation(value, bitCount, ::doArithmeticRightShift)
    final override fun logicalRightShift(value: T, bitCount: Int): T = performShiftOperation(value, bitCount, ::doLogicalRightShift)

    final override fun rotateLeft(value: T, bitCount: Int): T = performShiftOperation(value, bitCount, ::doRotateLeft)
    final override fun rotateRight(value: T, bitCount: Int): T = performShiftOperation(value, bitCount, ::doRotateRight)
}
