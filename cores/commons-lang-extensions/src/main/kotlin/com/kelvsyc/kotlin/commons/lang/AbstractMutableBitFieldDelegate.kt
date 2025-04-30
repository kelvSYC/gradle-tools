package com.kelvsyc.kotlin.commons.lang

import kotlin.properties.ReadWriteProperty
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty

/**
 * Abstract [ReadWriteProperty] implementation that delegates to a different property after applying a [BitField][org.apache.commons.lang3.BitField].
 *
 * @param H the holder type of the backing [BitField][org.apache.commons.lang3.BitField]
 * @param T the value type
 */
abstract class AbstractMutableBitFieldDelegate<H, T>(override val holder: KMutableProperty0<H>, mask: Int) : AbstractBitFieldDelegate<H, T>(holder, mask), ReadWriteProperty<Any, T> {
    /**
     * Sets the value in accordance to the bitfield.
     *
     * @param holder The value of the holder before the value is set
     * @param value The value to be set
     * @return The value of the holder after the value is set
     */
    abstract fun doSetValue(holder: H, value: T): H

    override fun setValue(thisRef: Any, property: KProperty<*>, value: T) {
        holder.set(doSetValue(holder.get(), value))
    }
}
