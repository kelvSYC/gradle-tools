package com.kelvsyc.kotlin.commons.numbers

import org.apache.commons.numbers.core.DD

/**
 * Delegating function converting this value to a [DD].
 *
 * @see DD.of
 */
fun Int.toDD(): DD = DD.of(this)
