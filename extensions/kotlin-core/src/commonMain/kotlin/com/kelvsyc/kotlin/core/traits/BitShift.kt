package com.kelvsyc.kotlin.core.traits

/**
 * Interface representing basic bit shifting operations on a type.
 *
 * Note that this interface does not impose the standard infix operations (`shl`, `shr`, etc.) on the type. This is
 * because types may have inherent infix operators or that types may have multiple implementations of this.
 *
 * @param T The type supporting bit shifting operations.
 */
interface BitShift<T> : LeftShift<T>, RightShift<T>, ArithmeticRightShift<T>
