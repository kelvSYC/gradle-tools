package com.kelvsyc.kotlin.core.traits

import kotlin.math.max

/**
 * [Bitwise] implementation for arrays of a particular [Bitwise] type.
 *
 * Operations may be performed on fixed-size arrays, or the size can be inferred based on the inputs. For all binary
 * operations, if no size is explicitly specified, the resulting array will be the same size as the larger of the two
 * arrays.
 *
 * If a size is explicitly specified, and a larger array is given as input, then any extra elements will be
 * ignored. If a smaller array is given as input, it will be extended to the specified size using zeroes after the
 * operation is performed.
 *
 * All operations return new arrays, and do not modify any of their inputs.
 *
 * @param size The size of the array to be returned from all operations. Defaults to `null`, which will create arrays
 *             based on the size of the inputs supplied.
 * @param arrayTraits Type traits for the array type.
 * @param elementTraits Type traits for the element type.
 * @param A The array type
 * @param E The element type
 */
abstract class AbstractArrayBitwise<A, E>(
    private val size: Int? = null,
    private val arrayTraits: ArrayLike<A, E>,
    private val elementTraits: Bitwise<E>
) : Bitwise<A> {
    /**
     * Element representing a "zero", a value where no bits are set.
     *
     * This value is used to fill arrays if a fixed size is requested, and all supplied inputs are arrays of smaller
     * size.
     */
    protected abstract val zero : E

    override fun and(lhs: A, rhs: A): A = arrayTraits.create(size ?: max(
        arrayTraits.getSize(lhs),
        arrayTraits.getSize(rhs)
    )
    ) {
        if (it < arrayTraits.getSize(lhs) && it < arrayTraits.getSize(rhs)) {
            elementTraits.and(arrayTraits.getAt(lhs, it), arrayTraits.getAt(rhs, it))
        } else {
            zero
        }
    }

    override fun or(lhs: A, rhs: A): A = arrayTraits.create(size ?: max(
        arrayTraits.getSize(lhs),
        arrayTraits.getSize(rhs)
    )
    ) {
        if (it < arrayTraits.getSize(lhs) && it < arrayTraits.getSize(rhs)) {
            elementTraits.or(arrayTraits.getAt(lhs, it), arrayTraits.getAt(rhs, it))
        } else if (it < arrayTraits.getSize(lhs)) {
            arrayTraits.getAt(lhs, it)
        } else if (it < arrayTraits.getSize(rhs)) {
            arrayTraits.getAt(rhs, it)
        } else {
            zero
        }
    }

    override fun xor(lhs: A, rhs: A): A = arrayTraits.create(size ?: max(
        arrayTraits.getSize(lhs),
        arrayTraits.getSize(rhs)
    )
    ) {
        if (it < arrayTraits.getSize(lhs) && it < arrayTraits.getSize(rhs)) {
            elementTraits.xor(arrayTraits.getAt(lhs, it), arrayTraits.getAt(rhs, it))
        } else if (it < arrayTraits.getSize(lhs)) {
            arrayTraits.getAt(lhs, it)
        } else if (it < arrayTraits.getSize(rhs)) {
            arrayTraits.getAt(rhs, it)
        } else {
            zero
        }
    }

    override fun inv(value: A): A = arrayTraits.create(size ?: arrayTraits.getSize(value)) {
        if (it < arrayTraits.getSize(value)) elementTraits.inv(arrayTraits.getAt(value, it)) else zero
    }
}
