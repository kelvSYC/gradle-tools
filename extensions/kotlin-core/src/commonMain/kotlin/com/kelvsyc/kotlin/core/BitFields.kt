@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.core

import com.kelvsyc.internal.kotlin.core.AbstractByteBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.AbstractIntBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.AbstractLongBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.AbstractShortBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.MutableByteBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.MutableIntBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.MutableLongBasedBitFieldDelegate
import com.kelvsyc.internal.kotlin.core.MutableShortBasedBitFieldDelegate
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0

fun flag(backingProperty: KProperty0<Byte>, off: Int) = bitfield(backingProperty, off, 1) {
    it.toInt() != 0
}

fun flag(backingProperty: KMutableProperty0<Byte>, off: Int) =
    bitfield(backingProperty, off, 1, Converter.of({it.toInt() != 0}, {if (it) 0x01 else 0x00}))

fun flag(backingProperty: KProperty0<Short>, off: Int) = bitfield(backingProperty, off, 1) {
    it.toInt() != 0
}

fun flag(backingProperty: KMutableProperty0<Short>, off: Int) =
    bitfield(backingProperty, off, 1, Converter.of({it.toInt() != 0}, {if (it) 0x01 else 0x00}))

fun flag(backingProperty: KProperty0<Int>, off: Int) = bitfield(backingProperty, off, 1) {
    it != 0
}

fun flag(backingProperty: KMutableProperty0<Int>, off: Int) =
    bitfield(backingProperty, off, 1, Converter.of({it != 0}, {if (it) 0x01 else 0x00}))

fun flag(backingProperty: KProperty0<Long>, off: Int) = bitfield(backingProperty, off, 1) {
    it != 0L
}

fun flag(backingProperty: KMutableProperty0<Long>, off: Int) =
    bitfield(backingProperty, off, 1, Converter.of({it != 0L}, {if (it) 0x01 else 0x00}))

fun <T> bitfield(backingProperty: KProperty0<Byte>, off: Int, len: Int, fn: (Byte) -> T) =
    object : AbstractByteBasedBitFieldDelegate<T>(backingProperty, off, len) {
        override fun convert(bits: Byte): T = fn(bits)
    }

fun bitfield(backingProperty: KProperty0<Byte>, off: Int, len: Int) = bitfield(backingProperty, off, len, {it})

fun <T> bitfield(backingProperty: KMutableProperty0<Byte>, off: Int, len: Int, converter: Converter<Byte, T>) =
    MutableByteBasedBitFieldDelegate(backingProperty, off, len, converter)

fun bitfield(backingProperty: KMutableProperty0<Byte>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, Converter.identity())

fun <T> bitfield(backingProperty: KProperty0<Short>, off: Int, len: Int, fn: (Short) -> T) =
    object : AbstractShortBasedBitFieldDelegate<T>(backingProperty, off, len) {
        override fun convert(bits: Short): T = fn(bits)
    }

fun bitfield(backingProperty: KProperty0<Short>, off: Int, len: Int) = bitfield(backingProperty, off, len, {it})

fun <T> bitfield(backingProperty: KMutableProperty0<Short>, off: Int, len: Int, converter: Converter<Short, T>) =
    MutableShortBasedBitFieldDelegate(backingProperty, off, len, converter)

fun bitfield(backingProperty: KMutableProperty0<Short>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, Converter.identity())

fun <T> bitfield(backingProperty: KProperty0<Int>, off: Int, len: Int, fn: (Int) -> T) =
    object : AbstractIntBasedBitFieldDelegate<T>(backingProperty, off, len) {
        override fun convert(bits: Int): T = fn(bits)
    }

fun bitfield(backingProperty: KProperty0<Int>, off: Int, len: Int) = bitfield(backingProperty, off, len, {it})

fun <T> bitfield(backingProperty: KMutableProperty0<Int>, off: Int, len: Int, converter: Converter<Int, T>) =
    MutableIntBasedBitFieldDelegate(backingProperty, off, len, converter)

fun bitfield(backingProperty: KMutableProperty0<Int>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, Converter.identity())

fun <T> bitfield(backingProperty: KProperty0<Long>, off: Int, len: Int, fn: (Long) -> T) =
    object : AbstractLongBasedBitFieldDelegate<T>(backingProperty, off, len) {
        override fun convert(bits: Long): T = fn(bits)
    }

fun bitfield(backingProperty: KProperty0<Long>, off: Int, len: Int) = bitfield(backingProperty, off, len, {it})

fun <T> bitfield(backingProperty: KMutableProperty0<Long>, off: Int, len: Int, converter: Converter<Long, T>) =
    MutableLongBasedBitFieldDelegate(backingProperty, off, len, converter)

fun bitfield(backingProperty: KMutableProperty0<Long>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, Converter.identity())
