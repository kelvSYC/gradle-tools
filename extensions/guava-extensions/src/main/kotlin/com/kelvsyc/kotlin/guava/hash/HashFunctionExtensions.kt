package com.kelvsyc.kotlin.guava.hash

import com.google.common.hash.HashCode
import com.google.common.hash.HashFunction
import com.google.common.hash.Hasher
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Produces a [HashCode] by applying actions to a [Hasher].
 */
@OptIn(ExperimentalContracts::class)
@Suppress("UnstableApiUsage")
fun HashFunction.hash(action: Hasher.() -> Unit): HashCode {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return newHasher().apply(action).hash()
}

/**
 * Produces a [HashCode] by applying actions to a [Hasher].
 *
 * @param expectedInputSize An estimation of the expected input size, in bytes.
 */
@OptIn(ExperimentalContracts::class)
@Suppress("UnstableApiUsage")
fun HashFunction.hash(expectedInputSize: Int, action: Hasher.() -> Unit): HashCode {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return newHasher(expectedInputSize).apply(action).hash()
}
