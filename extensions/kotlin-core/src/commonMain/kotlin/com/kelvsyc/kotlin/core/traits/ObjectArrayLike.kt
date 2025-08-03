package com.kelvsyc.kotlin.core.traits

/**
 * Creates an [ArrayLike] traits objects for an object array type.
 *
 * @param E The object type of the array
 */
inline fun <reified E> arrayLike() = object : ArrayLike<Array<E>, E> {
    override fun create(size: Int, init: (Int) -> E): Array<E> = Array(size, init)
    override fun getAt(array: Array<E>, index: Int): E = array[index]
    override fun getSize(array: Array<E>): Int = array.size

    override fun any(array: Array<E>, predicate: (E) -> Boolean): Boolean = array.any(predicate)
    override fun all(array: Array<E>, predicate: (E) -> Boolean): Boolean = array.all(predicate)
    override fun none(array: Array<E>, predicate: (E) -> Boolean): Boolean = array.none(predicate)

    override fun <R> map(array: Array<E>, transform: (E) -> R): List<R> = array.map(transform)
    override fun <R> mapIndexed(array: Array<E>, transform: (Int, E) -> R): List<R> = array.mapIndexed(transform)
    override fun <R> flatMap(array: Array<E>, transform: (E) -> Iterable<R>): List<R> = array.flatMap(transform)
    override fun <R> flatMapIndexed(array: Array<E>, transform: (Int, E) -> Iterable<R>): List<R> =
        array.flatMapIndexed(transform)

    override fun forEach(array: Array<E>, action: (E) -> Unit) = array.forEach(action)
    override fun forEachIndexed(array: Array<E>, action: (Int, E) -> Unit) = array.forEachIndexed(action)

    override fun indexOfFirst(array: Array<E>, predicate: (E) -> Boolean): Int = array.indexOfFirst(predicate)
    override fun indexOfLast(array: Array<E>, predicate: (E) -> Boolean): Int = array.indexOfLast(predicate)
}
