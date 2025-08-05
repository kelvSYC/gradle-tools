package com.kelvsyc.internal.kotlin.core.traits

import com.kelvsyc.kotlin.core.traits.Addition
import com.kelvsyc.kotlin.core.traits.IntegerArithmetic
import com.kelvsyc.kotlin.core.traits.IntegerDivision
import com.kelvsyc.kotlin.core.traits.Multiplication

object ByteIntegerArithmetic : IntegerArithmetic<Byte>,
        Addition<Byte> by ByteAddition,
        Multiplication<Byte> by ByteMultiplication,
        IntegerDivision<Byte> by ByteDivision

object UByteIntegerArithmetic : IntegerArithmetic<UByte>,
        Addition<UByte> by UByteAddition,
        Multiplication<UByte> by UByteMultiplication,
        IntegerDivision<UByte> by UByteDivision

object ShortIntegerArithmetic : IntegerArithmetic<Short>,
        Addition<Short> by ShortAddition,
        Multiplication<Short> by ShortMultiplication,
        IntegerDivision<Short> by ShortDivision

object UShortIntegerArithmetic : IntegerArithmetic<UShort>,
        Addition<UShort> by UShortAddition,
        Multiplication<UShort> by UShortMultiplication,
        IntegerDivision<UShort> by UShortDivision

object IntIntegerArithmetic : IntegerArithmetic<Int>,
        Addition<Int> by IntAddition,
        Multiplication<Int> by IntMultiplication,
        IntegerDivision<Int> by IntDivision

object UIntIntegerArithmetic : IntegerArithmetic<UInt>,
        Addition<UInt> by UIntAddition,
        Multiplication<UInt> by UIntMuliplication,
        IntegerDivision<UInt> by UIntDivision

object LongIntegerArithmetic : IntegerArithmetic<Long>,
        Addition<Long> by LongAddition,
        Multiplication<Long> by LongMultiplication,
        IntegerDivision<Long> by LongDivision

object ULongIntegerArithmetic : IntegerArithmetic<ULong>,
        Addition<ULong> by ULongAddition,
        Multiplication<ULong> by ULongMultiplication,
        IntegerDivision<ULong> by ULongDivision
