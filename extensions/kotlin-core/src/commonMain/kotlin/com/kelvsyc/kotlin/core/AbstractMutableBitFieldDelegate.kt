package com.kelvsyc.kotlin.core

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * Partial implementation of a delegated mutable property whose value is packed as bits contained in a different
 * property.
 *
 * Values supplied to the forward conversion, used in [getValue], will have already undergone the relevant bit shifts,
 * while values supplied to the reverse conversion, used in [setValue], will be subsequently bit shifted.
 *
 * @param backingProperty The backing property
 * @param off The bit offset of the value within the backing property
 * @param len The number of bits the value takes up within the backing property
 * @param converter The converter used to convert instances of the backing property to instances of the declared type.
 * @param T The declared type of this property
 * @param S The type of the backing property
 * @param B The type of the backing property's underlying bit store
 */
abstract class AbstractMutableBitFieldDelegate<S, T, B>(
    override val backingProperty: KMutableProperty0<S>,
    off: Int,
    len: Int,
    protected val converter: Converter<B, T>
) : AbstractBitFieldDelegate<S, T, B>(backingProperty, off, len), ReadWriteProperty<Any?, T> {
    final override fun convert(bits: B): T = converter(bits)

    override fun setValue(thisRef: Any?, property: KProperty<*>, value: T) {
        val baseValue = converter.reverse(value)
        val shifted = bitstore.leftShift(baseValue, off)
        val existingValue = backingProperty.get()
        val existingBits = bitsBased.converter(existingValue)
        val inverseMask = bitstore.inv(mask)
        val masked = bitstore.and(shifted, mask)
        val newValue = bitstore.or(bitstore.and(existingBits, inverseMask), masked)
        backingProperty.set(bitsBased.converter.reverse(newValue))
    }
}
