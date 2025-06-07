package com.kelvsyc.kotlin.guava.math

import com.kelvsyc.kotlin.math.FloatStore

/**
 * Returns `true` if this value represents a mathematical integer.
 */
val Float.isMathematicalInteger: Boolean
    get() = FloatStore.create(this).isMathematicalInteger

/**
 * Returns `true` if this value is exactly equal to `2^k` for some finite integer `k`.
 */
val Float.isPowerOfTwo: Boolean
    get() = FloatStore.create(this).isPowerOfTwo
