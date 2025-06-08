package com.kelvsyc.kotlin.commons.lang

import kotlin.reflect.KProperty0

/**
 * [ReadOnlyProperty][kotlin.properties.ReadOnlyProperty] implementation representing a value stored as a bit field
 * within a [Byte] property.
 */
class ByteBitFieldDelegate(holder: KProperty0<Byte>, mask: Int) : AbstractBitFieldDelegate<Byte, Byte>(holder, mask) {
    override fun doGetValue(holder: Byte): Byte = bitfield.getValue(holder.toInt()).toByte()
}

/**
 * [ReadOnlyProperty][kotlin.properties.ReadOnlyProperty] implementation representing a value stored as a bit field
 * within a [Short] property.
 */
class ShortBitFieldDelegate(holder: KProperty0<Short>, mask: Int) : AbstractBitFieldDelegate<Short, Short>(holder, mask) {
    override fun doGetValue(holder: Short): Short = bitfield.getShortValue(holder)
}

/**
 * [ReadOnlyProperty][kotlin.properties.ReadOnlyProperty] implementation representing a value stored as a bit field
 * within a [Int] property.
 */
class IntBitFieldDelegate(holder: KProperty0<Int>, mask: Int) : AbstractBitFieldDelegate<Int, Int>(holder, mask) {
    override fun doGetValue(holder: Int): Int = bitfield.getValue(holder)
}

/**
 * [ReadOnlyProperty][kotlin.properties.ReadOnlyProperty] implementation representing a boolean flag value within a
 * [Byte] property.
 */
class ByteFlagDelegate(holder: KProperty0<Byte>, mask: Int) : AbstractBitFieldDelegate<Byte, Boolean>(holder, mask) {
    override fun doGetValue(holder: Byte): Boolean = bitfield.isSet(holder.toInt())
}

/**
 * [ReadOnlyProperty][kotlin.properties.ReadOnlyProperty] implementation representing a boolean flag value within a
 * [Short] property.
 */
class ShortFlagDelegate(holder: KProperty0<Short>, mask: Int) : AbstractBitFieldDelegate<Short, Boolean>(holder, mask) {
    override fun doGetValue(holder: Short): Boolean = bitfield.isSet(holder.toInt())
}

/**
 * [ReadOnlyProperty][kotlin.properties.ReadOnlyProperty] implementation representing a boolean flag value within a
 * [Int] property.
 */
class IntFlagDelegate(holder: KProperty0<Int>, mask: Int) : AbstractBitFieldDelegate<Int, Boolean>(holder, mask) {
    override fun doGetValue(holder: Int): Boolean = bitfield.isSet(holder)
}
