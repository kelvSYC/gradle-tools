@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.core

import com.kelvsyc.internal.kotlin.core.AbstractByteBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.AbstractIntBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.AbstractLongBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.AbstractMutableByteBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.AbstractMutableIntBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.AbstractMutableLongBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.AbstractMutableShortBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.AbstractShortBasedBitFieldDelegate
import com.kelvsyc.kotlin.core.traits.BitsBased
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0

fun flag(backingProperty: KProperty0<Byte>, off: Int) = bitfield(backingProperty, off, 1, TypeTraits.Byte) {
    it.toInt() != 0
}

fun flag(backingProperty: KMutableProperty0<Byte>, off: Int) =
    bitfield(backingProperty, off, 1, TypeTraits.Byte, Converter.of({it.toInt() != 0}, {if (it) 0x01 else 0x00}))

fun flag(backingProperty: KProperty0<Short>, off: Int) = bitfield(backingProperty, off, 1, TypeTraits.Short) {
    it.toInt() != 0
}

fun flag(backingProperty: KMutableProperty0<Short>, off: Int) =
    bitfield(backingProperty, off, 1, TypeTraits.Short, Converter.of({it.toInt() != 0}, {if (it) 0x01 else 0x00}))

fun flag(backingProperty: KProperty0<Int>, off: Int) = bitfield(backingProperty, off, 1, TypeTraits.Int) {
    it != 0
}

fun flag(backingProperty: KMutableProperty0<Int>, off: Int) =
    bitfield(backingProperty, off, 1, TypeTraits.Int, Converter.of({it != 0}, {if (it) 0x01 else 0x00}))

fun flag(backingProperty: KProperty0<Long>, off: Int) = bitfield(backingProperty, off, 1, TypeTraits.Long) {
    it != 0L
}

fun flag(backingProperty: KMutableProperty0<Long>, off: Int) =
    bitfield(backingProperty, off, 1, TypeTraits.Long, Converter.of({it != 0L}, {if (it) 0x01 else 0x00}))

fun <S, T> bitfield(backingProperty: KProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Byte>, fn: (Byte) -> T) =
    object : AbstractByteBasedBitFieldDelegate<S, T>(backingProperty, off, len) {
        override val bitsBased: BitsBased<S, Byte> = bitsBased
        override fun convert(bits: Byte): T = fn(bits)
    }

fun bitfield(backingProperty: KProperty0<Byte>, off: Int, len: Int) = bitfield(backingProperty, off, len, TypeTraits.Byte, {it})

fun <S, T> bitfield(backingProperty: KMutableProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Byte>, converter: Converter<Byte, T>) =
    object: AbstractMutableByteBasedBitFieldDelegate<S, T>(backingProperty, off, len, converter) {
        override val bitsBased: BitsBased<S, Byte> = bitsBased
    }

fun bitfield(backingProperty: KMutableProperty0<Byte>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, TypeTraits.Byte, Converter.identity())

fun <S, T> bitfield(backingProperty: KProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Short>, fn: (Short) -> T) =
    object : AbstractShortBasedBitFieldDelegate<S, T>(backingProperty, off, len) {
        override val bitsBased: BitsBased<S, Short> = bitsBased
        override fun convert(bits: Short): T = fn(bits)
    }

fun bitfield(backingProperty: KProperty0<Short>, off: Int, len: Int) = bitfield(backingProperty, off, len, TypeTraits.Short, {it})

fun <S, T> bitfield(backingProperty: KMutableProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Short>, converter: Converter<Short, T>) =
    object : AbstractMutableShortBasedBitFieldDelegate<S, T>(backingProperty, off, len, converter) {
        override val bitsBased: BitsBased<S, Short> = bitsBased
    }

fun bitfield(backingProperty: KMutableProperty0<Short>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, TypeTraits.Short, Converter.identity())

fun <S, T> bitfield(backingProperty: KProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Int>, fn: (Int) -> T) =
    object : AbstractIntBasedBitFieldDelegate<S, T>(backingProperty, off, len) {
        override val bitsBased: BitsBased<S, Int> = bitsBased
        override fun convert(bits: Int): T = fn(bits)
    }

fun bitfield(backingProperty: KProperty0<Int>, off: Int, len: Int) = bitfield(backingProperty, off, len, TypeTraits.Int, {it})

fun <S, T> bitfield(backingProperty: KMutableProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Int>, converter: Converter<Int, T>) =
    object: AbstractMutableIntBasedBitFieldDelegate<S, T>(backingProperty, off, len, converter) {
        override val bitsBased: BitsBased<S, Int> = bitsBased
    }

fun bitfield(backingProperty: KMutableProperty0<Int>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, TypeTraits.Int, Converter.identity())

fun <S, T> bitfield(backingProperty: KProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Long>, fn: (Long) -> T) =
    object : AbstractLongBasedBitFieldDelegate<S, T>(backingProperty, off, len) {
        override val bitsBased: BitsBased<S, Long> = bitsBased
        override fun convert(bits: Long): T = fn(bits)
    }

fun bitfield(backingProperty: KProperty0<Long>, off: Int, len: Int) = bitfield(backingProperty, off, len, TypeTraits.Long, {it})

fun <S, T> bitfield(backingProperty: KMutableProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Long>, converter: Converter<Long, T>) =
    object: AbstractMutableLongBasedBitFieldDelegate<S, T>(backingProperty, off, len, converter) {
        override val bitsBased: BitsBased<S, Long> = bitsBased
    }

fun bitfield(backingProperty: KMutableProperty0<Long>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, TypeTraits.Long, Converter.identity())
