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
}
