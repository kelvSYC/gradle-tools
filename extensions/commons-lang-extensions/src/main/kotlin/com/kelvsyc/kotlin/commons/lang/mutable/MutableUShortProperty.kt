package com.kelvsyc.kotlin.commons.lang.mutable

import org.apache.commons.lang3.mutable.MutableShort
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Kotlin [ReadWriteProperty] implementation for a [UShort], backed by a [MutableShort].
 */
class MutableUShortProperty(initialValue: UShort) : ReadWriteProperty<Any, UShort> {
    private val inner = MutableShort(initialValue.toShort())

    override fun getValue(thisRef: Any, property: KProperty<*>): UShort = inner.toShort().toUShort()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: UShort) {
        inner.value = value.toShort()
    }
}
