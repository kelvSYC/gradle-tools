package com.kelvsyc.kotlin.commons.lang.mutable

import org.apache.commons.lang3.mutable.MutableByte
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Kotlin [ReadWriteProperty] implementation for a [UByte], backed by a [MutableByte].
 */
class MutableUByteProperty(initialValue: UByte) : ReadWriteProperty<Any, UByte> {
    private val inner = MutableByte(initialValue.toByte())

    override fun getValue(thisRef: Any, property: KProperty<*>): UByte = inner.toByte().toUByte()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: UByte) {
        inner.value = value.toByte()
    }
}
