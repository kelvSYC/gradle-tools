package com.kelvsyc.kotlin.guava.escape

import com.google.common.escape.Escapers

/**
 * Returns an [Escaper] created from populating an [Escapers.Builder] using the given action.
 */
fun buildEscaper(action: Escapers.Builder.() -> Unit) = Escapers.builder().apply(action).build()
