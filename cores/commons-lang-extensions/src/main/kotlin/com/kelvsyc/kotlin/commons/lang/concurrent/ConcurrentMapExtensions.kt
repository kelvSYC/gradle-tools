package com.kelvsyc.kotlin.commons.lang.concurrent

import org.apache.commons.lang3.concurrent.ConcurrentInitializer
import org.apache.commons.lang3.concurrent.ConcurrentUtils
import java.util.concurrent.ConcurrentMap

/**
 * Checks if the specified map contains a key, and creates a corresponding value using the specified [ConcurrentInitializer]
 * if it does not.
 */
fun <K, V> ConcurrentMap<K, V>.createIfAbsent(key: K, value: ConcurrentInitializer<V>) = ConcurrentUtils.createIfAbsent(this, key, value)

/**
 * Puts a value in the specified map if the key is not present. Note that this differs from [ConcurrentMap.putIfAbsent]
 * in that the operation is performed atomically.
 *
 * @see ConcurrentUtils.putIfAbsent
 */
fun <K, V> ConcurrentMap<K, V>.putIfAbsentAtomic(key: K, value: V): V? = ConcurrentUtils.putIfAbsent(this, key, value)
