package com.kelvsyc.kotlin.commons.numbers

import org.apache.commons.numbers.core.Addition

operator fun <T : Addition<T>> T.unaryPlus() = this

/**
 * Delegate function allowing for operator overload for the addable type.
 *
 * @see Addition.add
 */
operator fun <T : Addition<T>> T.plus(rhs: T): T = add(rhs)

/**
 * Delegate function allowing for operator overload for the addable type.
 *
 * @see Addition.negate
 */
operator fun <T : Addition<T>> T.unaryMinus() = negate()

/**
 * Function implementing operator overload for the addable type.
 *
 * Equivalent to [add][Addition.add]`(rhs(`[negate][Addition.negate]`()))`
 */
operator fun <T : Addition<T>> T.minus(rhs: T): T = add(-rhs)
