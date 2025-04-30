package com.kelvsyc.kotlin.commons.lang.mutable

import org.apache.commons.lang3.mutable.MutableLong
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Kotlin [ReadWriteProperty] implementation for a [ULong], backed by a [MutableLong].
 */
class MutableULongProperty(initialValue: ULong) : ReadWriteProperty<Any, ULong> {
    private val inner = MutableLong(initialValue.toLong())

    override fun getValue(thisRef: Any, property: KProperty<*>): ULong = inner.toLong().toULong()

    override fun setValue(thisRef: Any, property: KProperty<*>, value: ULong) {
        inner.value = value.toLong()
    }
}
