package com.kelvsyc.kotlin.core

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * Partial implementation of a delegated mutable property whose value is packed as bits contained in a different
 * property.
 *
 * @param backingProperty The backing property
 * @param off The bit offset of the value within the backing property
 * @param len The number of bits the value takes up within the backing property
 * @param T The declared type of this property
 * @param B The type of the backing property
 */
abstract class AbstractMutableBitFieldDelegate<T, B>(
    override val backingProperty: KMutableProperty0<B>,
    off: Int,
    len: Int
) : AbstractBitFieldDelegate<T, B>(backingProperty, off, len), ReadWriteProperty<Any?, T> {
    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val baseValue = converter.reverse(value)
        val shifted = bitShift.leftShift(baseValue, off)
        val existingValue = backingProperty.get()
        val inverseMask = bitwise.inv(mask)
        val masked = bitwise.and(shifted, mask)
        val newValue = bitwise.or(bitwise.and(existingValue, inverseMask), masked)
        backingProperty.set(newValue)
    }
}
