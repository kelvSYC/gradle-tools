package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.DiscreteDomain
import kotlin.enums.EnumEntries
import kotlin.enums.enumEntries

/**
 * [DiscreteDomain] implementation for an enum type.
 *
 * To obtain the [DiscreteDomain] for a given type, use [of].
 */
class EnumEntriesDomain<E : Enum<E>>(private val entries: EnumEntries<E>) : DiscreteDomain<E>() {
    companion object {
        inline fun <reified E : Enum<E>> of() = EnumEntriesDomain(enumEntries<E>())
    }

    override fun distance(start: E, end: E): Long = (end.ordinal - start.ordinal).toLong()

    override fun next(value: E): E? = entries.getOrNull(value.ordinal + 1)
    override fun previous(value: E): E? = entries.getOrNull(value.ordinal - 1)

    override fun maxValue(): E = entries.last()
    override fun minValue(): E = entries.first()
}
