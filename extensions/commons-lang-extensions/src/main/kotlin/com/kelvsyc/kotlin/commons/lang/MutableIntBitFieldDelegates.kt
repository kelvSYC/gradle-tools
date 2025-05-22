package com.kelvsyc.kotlin.commons.lang

import kotlin.reflect.KMutableProperty0

/**
 * [ReadWriteProperty][kotlin.properties.ReadWriteProperty] implementation representing a value stored as a bit field
 * within a [Byte] property.
 */
class MutableByteBitFieldDelegate(holder: KMutableProperty0<Byte>, mask: Int) : AbstractMutableBitFieldDelegate<Byte, Byte>(holder, mask) {
    override fun doGetValue(holder: Byte): Byte = bitfield.getValue(holder.toInt()).toByte()
    override fun doSetValue(holder: Byte, value: Byte): Byte = bitfield.setValue(holder.toInt(), value.toInt()).toByte()
}

/**
 * [ReadWriteProperty][kotlin.properties.ReadWriteProperty] implementation representing a value stored as a bit field
 * within a [Short] property.
 */
class MutableShortBitFieldDelegate(holder: KMutableProperty0<Short>, mask: Int) : AbstractMutableBitFieldDelegate<Short, Short>(holder, mask) {
    override fun doGetValue(holder: Short): Short = bitfield.getShortValue(holder)
    override fun doSetValue(holder: Short, value: Short): Short = bitfield.setShortValue(holder, value)
}

/**
 * [ReadWriteProperty][kotlin.properties.ReadWriteProperty] implementation representing a value stored as a bit field
 * within a [int] property.
 */
class MutableIntBitFieldDelegate(holder: KMutableProperty0<Int>, mask: Int) : AbstractMutableBitFieldDelegate<Int, Int>(holder, mask) {
    override fun doGetValue(holder: Int): Int = bitfield.getValue(holder)
    override fun doSetValue(holder: Int, value: Int): Int = bitfield.setValue(holder, value)
}

/**
 * [ReadWriteProperty][kotlin.properties.ReadWriteProperty] implementation representing a boolean flag value within a
 * [Byte] property.
 */
class MutableByteFlagDelegate(holder: KMutableProperty0<Byte>, mask: Int) : AbstractMutableBitFieldDelegate<Byte, Boolean>(holder, mask) {
    override fun doGetValue(holder: Byte): Boolean = bitfield.isSet(holder.toInt())
    override fun doSetValue(holder: Byte, value: Boolean): Byte = bitfield.setByteBoolean(holder, value)
}

/**
 * [ReadWriteProperty][kotlin.properties.ReadWriteProperty] implementation representing a boolean flag value within a
 * [Short] property.
 */
class MutableShortFlagDelegate(holder: KMutableProperty0<Short>, mask: Int) : AbstractMutableBitFieldDelegate<Short, Boolean>(holder, mask) {
    override fun doGetValue(holder: Short): Boolean = bitfield.isSet(holder.toInt())
    override fun doSetValue(holder: Short, value: Boolean): Short = bitfield.setShortBoolean(holder, value)
}

/**
 * [ReadWriteProperty][kotlin.properties.ReadWriteProperty] implementation representing a boolean flag value within a
 * [Int] property.
 */
class MutableIntFlagDelegate(holder: KMutableProperty0<Int>, mask: Int) : AbstractMutableBitFieldDelegate<Int, Boolean>(holder, mask) {
    override fun doGetValue(holder: Int): Boolean = bitfield.isSet(holder)
    override fun doSetValue(holder: Int, value: Boolean): Int = bitfield.setBoolean(holder, value)
}
