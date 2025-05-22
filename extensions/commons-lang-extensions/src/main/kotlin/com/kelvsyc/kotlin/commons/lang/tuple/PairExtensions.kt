package com.kelvsyc.kotlin.commons.lang.tuple

import org.apache.commons.lang3.tuple.Pair as CommonsPair
import org.apache.commons.lang3.tuple.Triple as CommonsTriple

/**
 * Returns this [Pair] as a Commons [Pair][CommonsPair].
 */
fun <A, B> Pair<A, B>.toCommonsPair() = CommonsPair.of(first, second)

/**
 * Returns this [Pair][CommonsPair] as a Kotlin [Pair].
 */
fun <L, R> CommonsPair<L, R>.toKotlinPair() = left to right

/**
 * Returns this [Triple] as a Commons [Triple][CommonsTriple].
 */
fun <A, B, C> Triple<A, B, C>.toCommonsTriple() = CommonsTriple.of(first, second, third)

/**
 * Returns this [Triple][CommonsTriple] as a Kotlin [Triple].
 */
fun <L, M, R> CommonsTriple<L, M, R>.toKotlinTriple() = Triple(left, middle, right)
