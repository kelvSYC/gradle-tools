package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.BitCollection
import com.kelvsyc.kotlin.core.BitShift
import com.kelvsyc.kotlin.core.Bitwise

/**
 * Type trait denoting that the type can be used as a bit store.
 *
 * A bit store must be able to store bits, as defined in [BitCollection], and perform bitwise operations, defined in
 * [Bitwise] and [BitShift].
 *
 * @param The bit store type.
 */
interface BitStore<T> : BitCollection<T>, Bitwise<T>, BitShift<T>
