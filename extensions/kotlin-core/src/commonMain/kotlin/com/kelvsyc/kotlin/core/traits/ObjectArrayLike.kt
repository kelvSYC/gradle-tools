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
}
