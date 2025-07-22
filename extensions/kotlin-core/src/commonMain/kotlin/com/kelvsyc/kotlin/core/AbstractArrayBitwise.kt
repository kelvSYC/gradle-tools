package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.ArrayLike
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
 * @param A The array type
 * @param E The element type
 */
abstract class AbstractArrayBitwise<A, E>(private val size: Int? = null) : Bitwise<A> {
    /**
     * Type traits for the array type.
     */
    protected abstract val traits: ArrayLike<A, E>

    /**
     * Type traits for the element type.
     */
    protected abstract val base: Bitwise<E>

    /**
     * Element representing a "zero", a value where no bits are set.
     *
     * This value is used to fill arrays if a fixed size is requested, and all supplied inputs are arrays of smaller
     * size.
     */
    protected abstract val zero : E

    override fun and(lhs: A, rhs: A): A = traits.create(size ?: max(traits.getSize(lhs), traits.getSize(rhs))) {
        if (it < traits.getSize(lhs) && it < traits.getSize(rhs)) {
            base.and(traits.getAt(lhs, it), traits.getAt(rhs, it))
        } else {
            zero
        }
    }

    override fun or(lhs: A, rhs: A): A = traits.create(size ?: max(traits.getSize(lhs), traits.getSize(rhs))) {
        if (it < traits.getSize(lhs) && it < traits.getSize(rhs)) {
            base.or(traits.getAt(lhs, it), traits.getAt(rhs, it))
        } else if (it < traits.getSize(lhs)) {
            traits.getAt(lhs, it)
        } else if (it < traits.getSize(rhs)) {
            traits.getAt(rhs, it)
        } else {
            zero
        }
    }

    override fun xor(lhs: A, rhs: A): A = traits.create(size ?: max(traits.getSize(lhs), traits.getSize(rhs))) {
        if (it < traits.getSize(lhs) && it < traits.getSize(rhs)) {
            base.xor(traits.getAt(lhs, it), traits.getAt(rhs, it))
        } else if (it < traits.getSize(lhs)) {
            traits.getAt(lhs, it)
        } else if (it < traits.getSize(rhs)) {
            traits.getAt(rhs, it)
        } else {
            zero
        }
    }

    override fun inv(value: A): A = traits.create(size ?: traits.getSize(value)) {
        if (it < traits.getSize(value)) base.inv(traits.getAt(value, it)) else zero
    }
}
