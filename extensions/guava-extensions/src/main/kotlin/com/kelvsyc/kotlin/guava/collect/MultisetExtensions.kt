@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.EnumMultiset
import com.google.common.collect.HashMultiset
import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.LinkedHashMultiset
import com.google.common.collect.Multiset
import com.google.common.collect.TreeMultiset

/**
 * Builds a new read-only [Multiset] by populating an [ImmutableMultiset.Builder] using the given [action].
 */
fun <E : Any> buildMultiset(action: ImmutableMultiset.Builder<E>.() -> Unit): Multiset<E> =
    ImmutableMultiset.builder<E>().apply(action).build()

/**
 * Returns an empty [Multiset].
 *
 * @see ImmutableMultiset.of
 */
fun <E : Any> emptyMultiset(): Multiset<E> = ImmutableMultiset.of()

/**
 * Returns an empty [Multiset].
 *
 * @see ImmutableMultiset.of
 */
fun <E : Any> multisetOf(): Multiset<E> = ImmutableMultiset.of()

/**
 * Returns a new read-only [Multiset] containing the specified element.
 *
 * @see ImmutableMultiset.of
 */
fun <E : Any> multisetOf(element: E): Multiset<E> = ImmutableMultiset.of(element)

/**
 * Returns a new read-only [Multiset] containing the given elements.
 *
 * @see ImmutableMultiset.of
 */
fun <E : Any> multisetOf(e1: E, e2: E): Multiset<E> = ImmutableMultiset.of(e1, e2)

/**
 * Returns a new read-only [Multiset] containing the given elements.
 *
 * @see ImmutableMultiset.of
 */
fun <E : Any> multisetOf(e1: E, e2: E, e3: E): Multiset<E> = ImmutableMultiset.of(e1, e2, e3)

/**
 * Returns a new read-only [Multiset] containing the given elements.
 *
 * @see ImmutableMultiset.of
 */
fun <E : Any> multisetOf(e1: E, e2: E, e3: E, e4: E): Multiset<E> = ImmutableMultiset.of(e1, e2, e3, e4)

/**
 * Returns a new read-only [Multiset] containing the given elements.
 *
 * @see ImmutableMultiset.of
 */
fun <E : Any> multisetOf(e1: E, e2: E, e3: E, e4: E, e5: E): Multiset<E> = ImmutableMultiset.of(e1, e2, e3, e4, e5)

/**
 * Returns a new read-only [Multiset] with the given elements.
 */
fun <E : Any> multisetOf(vararg elements: E): Multiset<E> = buildMultiset {
    addAll(elements.asIterable())
}

/**
 * Returns a new [EnumMultiset].
 */
inline fun <reified E : Enum<E>> enumMultisetOf() = EnumMultiset.create(E::class.java)

/**
 * Returns a new [EnumMultiset] with the given elements.
 */
inline fun <reified E : Enum<E>> enumMultisetOf(vararg elements: E) = EnumMultiset.create(elements.asIterable(), E::class.java)

/**
 * Returns a new [HashMultiset].
 */
fun <E> hashMultisetOf() = HashMultiset.create<E>()

/**
 * Returns a new [HashMultiset] with the given elements.
 */
fun <E> hashMultisetOf(vararg elements: E) = HashMultiset.create(elements.asIterable())

/**
 * Returns a new [LinkedHashMultiset].
 */
fun <E> linkedMultisetOf() = LinkedHashMultiset.create<E>()

/**
 * Returns a new [LinkedHashMultiset] with the given elements.
 */
fun <E> linkedMultisetOf(vararg elements: E) = LinkedHashMultiset.create(elements.asIterable())

/**
 * Returns a new [TreeMultiset] sorted according to the natural order of the elements.
 */
fun <E : Comparable<E>> treeMultisetOf() = TreeMultiset.create<E>()

/**
 * Returns a new [TreeMultiset] sorted according to the specified comparator.
 */
fun <E> treeMultisetOf(comparator: Comparator<in E>) = TreeMultiset.create(comparator)

/**
 * Returns a new [TreeMultiset] sorted according to the natural order of the elements, with the given elements.
 */
fun <E: Comparable<E>> treeMultisetOf(vararg elements: E) = TreeMultiset.create(elements.asIterable())

/**
 * Returns a new [TreeMultiset] sorted according to the specified comparator, with the given elements.
 */
fun <E> treeMultisetOf(comparator: Comparator<in E>, vararg elements: E) = treeMultisetOf(comparator).also {
    it.addAll(elements.asList())
}
