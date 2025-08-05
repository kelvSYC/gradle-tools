package com.kelvsyc.kotlin.core.traits

/**
 * Marker interface denoting that a type supports the basic integer arithmetic operations.
 */
interface IntegerArithmetic<T> : Addition<T>, Multiplication<T>, IntegerDivision<T>
