package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.BitStore
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
    init {
        // FIXME pass in bitstore in the constructor to enable these checks
//        require(off >= 0 && off < bitstore.sizeBits) { "Offset must be in range" }
//        require(off > 0 && off + len <= bitstore.sizeBits) { "Length must be in range" }
    }

    /**
     * Object providing information on converting the backing type to its underlying bit store
     */
    protected abstract val bitsBased: BitsBased<S, B>

    /**
     * Object providing bit store traits needed for operations on the bit store
     */
    protected abstract val bitstore: BitStore<B>

    /**
     * The bit mask used to extract the value from the backing property.
     */
    @OptIn(ExperimentalStdlibApi::class)
    protected val mask: B by lazy {
        bitstore.fromBits(off ..< off + len)
    }

    /**
     * Converts an instance of the backing type to an instance of the declared type.
     */
    protected abstract fun convert(bits: B): T

    override fun getValue(thisRef: Any?, property: KProperty<*>): T {
        val baseValue = backingProperty.get()
        val bits = bitsBased.converter(baseValue)
        val masked = bitstore.and(bits, mask)
        val shifted = bitstore.rightShift(masked, off)
        return convert(shifted)
    }
}
