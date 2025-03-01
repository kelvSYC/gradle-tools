package com.kelvsyc.gradle

import org.gradle.api.NamedDomainObjectCollection

/**
 * Constructs a lazy sequence containing all of the elements of this collection.
 */
fun <T : Any> NamedDomainObjectCollection<T>.asSequence() = sequence {
    yieldAll(names.map { named(it) })
}
