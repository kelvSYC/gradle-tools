package com.kelvsyc.kotlin.commons.lang.mutable

import org.apache.commons.lang3.mutable.MutableInt
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Kotlin [ReadWriteProperty] implementation for a [UInt], backed by a [MutableInt].
 */
class MutableUIntProperty(initialValue: UInt) : ReadWriteProperty<Any, UInt> {
    private val inner = MutableInt(initialValue.toInt())

    override fun getValue(thisRef: Any, property: KProperty<*>): UInt = inner.toInt().toUInt()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: UInt) {
        inner.value = value.toInt()
    }
}
