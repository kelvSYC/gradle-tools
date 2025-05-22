package com.kelvsyc.kotlin.commons.lang

import org.apache.commons.lang3.concurrent.AtomicInitializer
import org.apache.commons.lang3.concurrent.AtomicSafeInitializer
import org.apache.commons.lang3.concurrent.ConstantInitializer

/**
 * Creates a [ConcurrentInitializingDelegate] from a simple [AtomicInitializer].
 *
 * @param fn the implementation of [AtomicInitializer.initialize]
 */
fun <T> atomic(fn: () -> T) = ConcurrentInitializingDelegate(object : AtomicInitializer<T>() {
    override fun initialize(): T = fn()
})

/**
 * Creates a [ConcurrentInitializingDelegate] from a simple [AtomicSafeInitializer].
 *
 * @param fn the implementation of [AtomicSafeInitializer.initialize]
 */
fun <T> atomicSafe(fn: () -> T) = ConcurrentInitializingDelegate(object : AtomicSafeInitializer<T>() {
    override fun initialize(): T = fn()
})

/**
 * Creates a [ConcurrentInitializingDelegate] from a simple [ConstantInitializer].
 *
 * This is generally redundant compared to simple assignment, but may be useful in teesting scenarios.
 *
 * @param obj the constant value to initialize
 */
fun <T> constant(obj: T) = ConcurrentInitializingDelegate(ConstantInitializer<T>(obj))
