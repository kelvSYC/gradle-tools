package com.kelvsyc.kotlin.guava.escape

import com.google.common.escape.Escaper
import com.google.common.escape.Escapers
import kotlin.contracts.ExperimentalContracts
import kotlin.contracts.InvocationKind
import kotlin.contracts.contract

/**
 * Returns an [Escaper] created from populating an [Escapers.Builder] using the given action.
 */
@OptIn(ExperimentalContracts::class)
fun buildEscaper(action: Escapers.Builder.() -> Unit): Escaper {
    contract {
        callsInPlace(action, InvocationKind.EXACTLY_ONCE)
    }
    return Escapers.builder().apply(action).build()
}
