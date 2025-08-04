package com.kelvsyc.kotlin.core.traits

/**
 * Partial implementation of [Addition] where [subtraction][subtract] is implemented through adding the negation of
 * the right-hand argument.
 */
abstract class AbstractSignedAddition<T>(private val signed: Signed<T>) : Addition<T> {
    override fun subtract(lhs: T, rhs: T): T = add(lhs, signed.negate(rhs))
}
