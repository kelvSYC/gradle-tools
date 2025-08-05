package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Addition
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic
import com.kelvsyc.kotlin.core.traits.FloatingPointDivision
import com.kelvsyc.kotlin.core.traits.Multiplication

object FloatFloatingPointArithmetic : FloatingPointArithmetic<Float>,
        Addition<Float> by FloatAddition,
        Multiplication<Float> by FloatMultiplication,
        FloatingPointDivision<Float> by FloatDivision

object DoubleFloatingPointArithmetic : FloatingPointArithmetic<Double>,
        Addition<Double> by DoubleAddition,
        Multiplication<Double> by DoubleMultiplication,
        FloatingPointDivision<Double> by DoubleDivision
