package com.kelvsyc.kotlin.core.traits

/**
 * Trait interface denoting that a type behaves like an [Array].
 *
 * @param A The array type
 * @param E The element type
 */
interface ArrayLike<A, E> {
    /**
     * Creates an array, and initializes it with the supplied function.
     *
     * @param size The size of the array to be created
     * @param init The initialization function, transforming the array index to the desired value.
     */
    fun create(size: Int, init: (Int) -> E): A

    /**
     * Retrieves an element from the supplied array.
     */
    fun getAt(array: A, index: Int): E

    /**
     * Retrieves the size of the supplied array.
     */
    fun getSize(array: A): Int

    /**
     * Returns `true` if at least one element in the array matches the specified predicate.
     */
    fun any(array: A, predicate: (E) -> Boolean): Boolean

    /**
     * Returns `true` if every element in the array matches the specified predicate.
     */
    fun all(array: A, predicate: (E) -> Boolean): Boolean

    /**
     * Returns `true` if none of the elements in the array matches the specified predicate.
     */
    fun none(array: A, predicate: (E) -> Boolean): Boolean

    fun <R> map(array: A, transform: (E) -> R): List<R>

    fun <R> mapIndexed(array: A, transform: (index: Int, E) -> R): List<R>

    fun <R> flatMap(array: A, transform: (E) -> Iterable<R>): List<R>

    fun <R> flatMapIndexed(array: A, transform: (index: Int, E) -> Iterable<R>): List<R>

    /**
     * Performs the supplied action for each item in the array.
     */
    fun forEach(array: A, action: (E) -> Unit)

    /**
     * Performs the supplied action for each item in the array. The index of the array is also supplied.
     */
    fun forEachIndexed(array: A, action: (index: Int, E) -> Unit)

    /**
     * Returns the index of the first element matching the array, or `-1` if no such element exists.
     */
    fun indexOfFirst(array: A, predicate: (E) -> Boolean): Int

    /**
     * Returns the index of the last element matching the array, or `-1` if no such element exists.
     */
    fun indexOfLast(array: A, predicate: (E) -> Boolean): Int
}
