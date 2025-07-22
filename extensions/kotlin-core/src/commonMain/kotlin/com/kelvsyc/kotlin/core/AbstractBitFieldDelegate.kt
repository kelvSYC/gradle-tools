package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.BitsBased
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

/**
 * Partial implementation of a delegated property whose value is packed as bits contained in a different property.
 *
 * @param backingProperty The backing property
 * @param off The bit offset of the value within the backing property
 * @param len The number of bits the value takes up within the backing property
 * @param T The declared type of this property
 * @param S The type of the backing property
 * @param B The type of the backing property's underlying bit store
 */
abstract class AbstractBitFieldDelegate<S, T, B>(
    protected open val backingProperty: KProperty0<S>,
    protected val off: Int,
    protected val len: Int
) : ReadOnlyProperty<Any?, T> {
    /**
     * Object providing information on converting the backing type to its underlying bit store
     */
    protected abstract val bitsBased: BitsBased<S, B>

    /**
     * Object providing the bit shifting operations needed for the conversion.
     */
    protected abstract val bitShift: BitShift<B>

    /**
     * Object providing the bit masking operations needed for the conversion.
     */
    protected abstract val bitwise: Bitwise<B>

    /**
     * The bit mask used to extract the value from the backing property.
     */
    protected val mask: B by lazy {
        getMask(off, len)
    }

    /**
     * Converts an instance of the backing type to an instance of the declared type.
     */
    protected abstract fun convert(bits: B): T

    /**
     * Retrieves the bit mask used to mask the value from the backing property.
     */
    protected abstract fun getMask(offset: Int, length: Int): B

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val baseValue = backingProperty.get()
        val bits = bitsBased.converter(baseValue)
        val masked = bitwise.and(bits, mask)
        val shifted = bitShift.rightShift(masked, off)
        return convert(shifted)
    }
}
