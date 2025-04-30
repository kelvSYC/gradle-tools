package com.kelvsyc.kotlin.commons.lang

import org.apache.commons.lang3.BitField
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty
import kotlin.reflect.KProperty0

/**
 * Abstract [ReadOnlyProperty] implementation that delegates to a different property after applying a [BitField][org.apache.commons.lang3.BitField].
 *
 * @param H the holder type of the backing [BitField][org.apache.commons.lang3.BitField]
 * @param T the value type
 */
abstract class AbstractBitFieldDelegate<H, T>(protected open val holder: KProperty0<H>, mask: Int) : ReadOnlyProperty<Any, T> {
    protected val bitfield: BitField = BitField(mask)

    /**
     * Retrieves the value from the holder.
     *
     * @param holder The value of the holder at the time the value is to be retrieved.
     */
    abstract fun doGetValue(holder: H): T

    override fun getValue(thisRef: Any, property: KProperty<*>): T = doGetValue(holder.get())
}
