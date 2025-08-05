package com.kelvsyc.kotlin.core.traits

/**
 * Marker interface denoting that a type supports the basic floating-point arithmetic operations.
 */
interface FloatingPointArithmetic<T> : Addition<T>, Multiplication<T>, FloatingPointDivision<T>
