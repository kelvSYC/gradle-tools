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
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0

fun flag(backingProperty: KProperty0<Byte>, off: Int) =
    object : AbstractByteBasedBitFieldDelegate<Boolean>(backingProperty, off, 1) {
        override val converter: Converter<Byte, Boolean>
            get() = Converter.of({it.toInt() != 0}, {if (it) 0x01 else 0x00})
    }

fun flag(backingProperty: KMutableProperty0<Byte>, off: Int) =
    object : AbstractMutableByteBasedBitFieldDelegate<Boolean>(backingProperty, off, 1) {
        override val converter: Converter<Byte, Boolean>
            get() = Converter.of({it.toInt() != 0}, {if (it) 0x01 else 0x00})
    }

fun flag(backingProperty: KProperty0<Short>, off: Int) =
    object : AbstractShortBasedBitFieldDelegate<Boolean>(backingProperty, off, 1) {
        override val converter: Converter<Short, Boolean>
            get() = Converter.of({it.toInt() != 0}, {if (it) 0x01 else 0x00})
    }

fun flag(backingProperty: KMutableProperty0<Short>, off: Int) =
    object : AbstractMutableShortBasedBitFieldDelegate<Boolean>(backingProperty, off, 1) {
        override val converter: Converter<Short, Boolean>
            get() = Converter.of({it.toInt() != 0}, {if (it) 0x01 else 0x00})
    }

fun flag(backingProperty: KProperty0<Int>, off: Int) =
    object : AbstractIntBasedBitFieldDelegate<Boolean>(backingProperty, off, 1) {
        override val converter: Converter<Int, Boolean>
            get() = Converter.of({it != 0}, {if (it) 0x01 else 0x00})
    }

fun flag(backingProperty: KMutableProperty0<Int>, off: Int) =
    object : AbstractMutableIntBasedBitFieldDelegate<Boolean>(backingProperty, off, 1) {
        override val converter: Converter<Int, Boolean>
            get() = Converter.of({it != 0}, {if (it) 0x01 else 0x00})
    }

fun flag(backingProperty: KProperty0<Long>, off: Int) =
    object : AbstractLongBasedBitFieldDelegate<Boolean>(backingProperty, off, 1) {
        override val converter: Converter<Long, Boolean>
            get() = Converter.of({it != 0L}, {if (it) 0x01 else 0x00})
    }

fun flag(backingProperty: KMutableProperty0<Long>, off: Int) =
    object : AbstractMutableLongBasedBitFieldDelegate<Boolean>(backingProperty, off, 1) {
        override val converter: Converter<Long, Boolean>
            get() = Converter.of({it != 0L}, {if (it) 0x01 else 0x00})
    }

fun bitfield(backingProperty: KProperty0<Byte>, off: Int, len: Int) =
    object : AbstractByteBasedBitFieldDelegate<Byte>(backingProperty, off, len) {
        override val converter: Converter<Byte, Byte>
            get() = Converter.identity()
    }

fun bitfield(backingProperty: KMutableProperty0<Byte>, off: Int, len: Int) =
    object : AbstractMutableByteBasedBitFieldDelegate<Byte>(backingProperty, off, len) {
        override val converter: Converter<Byte, Byte>
            get() = Converter.identity()
    }

fun bitfield(backingProperty: KProperty0<Short>, off: Int, len: Int) =
    object : AbstractShortBasedBitFieldDelegate<Short>(backingProperty, off, len) {
        override val converter: Converter<Short, Short>
            get() = Converter.identity()
    }

fun bitfield(backingProperty: KMutableProperty0<Short>, off: Int, len: Int) =
    object : AbstractMutableShortBasedBitFieldDelegate<Short>(backingProperty, off, len) {
        override val converter: Converter<Short, Short>
            get() = Converter.identity()
    }

fun bitfield(backingProperty: KProperty0<Int>, off: Int, len: Int) =
    object : AbstractIntBasedBitFieldDelegate<Int>(backingProperty, off, len) {
        override val converter: Converter<Int, Int>
            get() = Converter.identity()
    }

fun bitfield(backingProperty: KMutableProperty0<Int>, off: Int, len: Int) =
    object : AbstractMutableIntBasedBitFieldDelegate<Int>(backingProperty, off, len) {
        override val converter: Converter<Int, Int>
            get() = Converter.identity()
    }

fun bitfield(backingProperty: KProperty0<Long>, off: Int, len: Int) =
    object : AbstractLongBasedBitFieldDelegate<Long>(backingProperty, off, len) {
        override val converter: Converter<Long, Long>
            get() = Converter.identity()
    }

fun bitfield(backingProperty: KMutableProperty0<Long>, off: Int, len: Int) =
    object : AbstractMutableLongBasedBitFieldDelegate<Long>(backingProperty, off, len) {
        override val converter: Converter<Long, Long>
            get() = Converter.identity()
    }
