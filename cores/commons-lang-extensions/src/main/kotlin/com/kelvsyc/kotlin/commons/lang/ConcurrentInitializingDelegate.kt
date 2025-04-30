package com.kelvsyc.kotlin.commons.lang

import org.apache.commons.lang3.concurrent.ConcurrentInitializer
import kotlin.properties.ReadOnlyProperty
import kotlin.reflect.KProperty

/**
 * Kotlin [ReadOnlyProperty] implementation backed by a [ConcurrentInitializer].
 *
 * This allows [ConcurrentInitializer] instances to be used with Kotlin's delegated properties.
 */
class ConcurrentInitializingDelegate<T>(private val initializer: ConcurrentInitializer<T>) : ReadOnlyProperty<Any, T> {
    override fun getValue(thisRef: Any, property: KProperty<*>): T = initializer.get()
}
