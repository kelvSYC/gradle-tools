package com.kelvsyc.kotlin.commons.lang.mutable

import org.apache.commons.lang3.mutable.MutableObject
import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KProperty

/**
 * Kotlin [ReadWriteProperty] implementation backed by a [MutableObject].
 *
 * This allows [MutableObject] instances to be used with Kotlin's delegated properties. `null` values are permitted; for
 * a version that does not permit `null`, use [MutableProperty].
 */
class MutableNullableProperty<T> : ReadWriteProperty<Any, T?> {
    private val value: MutableObject<T> = MutableObject<T>()

    /**
     * Constructs this property with an initial non-`null` value.
     */
    constructor(initialValue: T) {
        value.value = initialValue
    }

    override fun getValue(thisRef: Any, property: KProperty<*>): T? = value.value

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T?) {
        this.value.value = value
    }
}
