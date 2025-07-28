package com.kelvsyc.kotlin.core

import com.kelvsyc.internal.kotlin.core.traits.ByteArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.ByteBitRotate
import com.kelvsyc.internal.kotlin.core.traits.ByteBitStore
import com.kelvsyc.internal.kotlin.core.traits.ByteBitsBased
import com.kelvsyc.internal.kotlin.core.traits.ByteRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.ByteStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.DoubleBitsBased
import com.kelvsyc.internal.kotlin.core.traits.FloatBitsBased
import com.kelvsyc.internal.kotlin.core.traits.IntArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.IntBitRotate
import com.kelvsyc.internal.kotlin.core.traits.IntBitStore
import com.kelvsyc.internal.kotlin.core.traits.IntBitsBased
import com.kelvsyc.internal.kotlin.core.traits.IntRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.IntStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.LongArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.LongBitRotate
import com.kelvsyc.internal.kotlin.core.traits.LongBitStore
import com.kelvsyc.internal.kotlin.core.traits.LongBitsBased
import com.kelvsyc.internal.kotlin.core.traits.LongRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.LongStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.ShortArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.ShortBitRotate
import com.kelvsyc.internal.kotlin.core.traits.ShortBitStore
import com.kelvsyc.internal.kotlin.core.traits.ShortBitsBased
import com.kelvsyc.internal.kotlin.core.traits.ShortRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.ShortStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.UByteArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.UByteBitRotate
import com.kelvsyc.internal.kotlin.core.traits.UByteBitStore
import com.kelvsyc.internal.kotlin.core.traits.UByteBitsBased
import com.kelvsyc.internal.kotlin.core.traits.UByteRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.UByteStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.UIntArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.UIntBitRotate
import com.kelvsyc.internal.kotlin.core.traits.UIntBitStore
import com.kelvsyc.internal.kotlin.core.traits.UIntBitsBased
import com.kelvsyc.internal.kotlin.core.traits.UIntRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.UIntStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.ULongArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.ULongBitRotate
import com.kelvsyc.internal.kotlin.core.traits.ULongBitStore
import com.kelvsyc.internal.kotlin.core.traits.ULongBitsBased
import com.kelvsyc.internal.kotlin.core.traits.ULongRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.ULongStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.UShortArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.UShortBitRotate
import com.kelvsyc.internal.kotlin.core.traits.UShortBitStore
import com.kelvsyc.internal.kotlin.core.traits.UShortBitsBased
import com.kelvsyc.internal.kotlin.core.traits.UShortRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.UShortStickyRightShift
import com.kelvsyc.kotlin.core.traits.ArrayLike
import com.kelvsyc.kotlin.core.traits.Binary32Traits
import com.kelvsyc.kotlin.core.traits.Binary64Traits
import com.kelvsyc.kotlin.core.traits.BitStore
import com.kelvsyc.kotlin.core.traits.BitsBased
import com.kelvsyc.kotlin.core.traits.RoundingRightShift
import com.kelvsyc.kotlin.core.traits.StickyRightShift
import kotlin.math.absoluteValue
import kotlin.Byte as KByte
import kotlin.ByteArray as KByteArray
import kotlin.Double as KDouble
import kotlin.Float as KFloat
import kotlin.Int as KInt
import kotlin.IntArray as KIntArray
import kotlin.Long as KLong
import kotlin.LongArray as KLongArray
import kotlin.Short as KShort
import kotlin.ShortArray as KShortArray
import kotlin.UByte as KUByte
import kotlin.UByteArray as KUByteArray
import kotlin.UInt as KUInt
import kotlin.UIntArray as KUIntArray
import kotlin.ULong as KULong
import kotlin.ULongArray as KULongArray
import kotlin.UShort as KUShort
import kotlin.UShortArray as KUShortArray

/**
 * Object holder for type traits for common Kotlin types.
 */
object TypeTraits {
    /**
     * Traits object for the [Byte][KByte] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object Byte :
        BitsBased<KByte, KByte> by ByteBitsBased,
        BitStore<KByte> by ByteBitStore,
        StickyRightShift<KByte> by ByteStickyRightShift,
        RoundingRightShift<KByte> by ByteRoundingRightShift,
        Addition<KByte>, Multiplication<KByte>,
        ByteBitRotate,
        Signed<KByte> {
        // Multiple interfaces define it, so we override explicitly
        override val sizeBits: KInt by KByte.Companion::SIZE_BITS

        override fun add(lhs: KByte, rhs: KByte): KByte = (lhs + rhs).toByte()
        override fun subtract(lhs: KByte, rhs: KByte): KByte = (lhs - rhs).toByte()

        override fun multiply(lhs: KByte, rhs: KByte): KByte = (lhs * rhs).toByte()
        override fun divide(lhs: KByte, rhs: KByte): KByte = (lhs / rhs).toByte()

        override fun isPositive(value: KByte): Boolean = value > 0
        override fun isNegative(value: KByte): Boolean = value < 0
        override fun negate(value: KByte): KByte = (-value).toByte()
        override fun absoluteValue(value: KByte): KByte = value.toInt().absoluteValue.toByte()
    }

    /**
     * Traits object for the [UByte][KUByte] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object UByte :
        BitsBased<KUByte, KByte> by UByteBitsBased,
        BitStore<KUByte> by UByteBitStore,
        StickyRightShift<KUByte> by UByteStickyRightShift,
        RoundingRightShift<KUByte> by UByteRoundingRightShift,
        Addition<KUByte>, Multiplication<KUByte>,
        UByteBitRotate {
        // Multiple interfaces define it, so we override explicitly
        override val sizeBits: KInt by KUByte.Companion::SIZE_BITS

        override fun add(lhs: KUByte, rhs: KUByte): KUByte = (lhs + rhs).toUByte()
        override fun subtract(lhs: KUByte, rhs: KUByte): KUByte = (lhs - rhs).toUByte()

        override fun multiply(lhs: KUByte, rhs: KUByte): KUByte = (lhs * rhs).toUByte()
        override fun divide(lhs: KUByte, rhs: KUByte): KUByte = (lhs / rhs).toUByte()
    }

    /**
     * Traits object for the [Short][KShort] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object Short :
        BitsBased<KShort, KShort> by ShortBitsBased,
        BitStore<KShort> by ShortBitStore,
        StickyRightShift<KShort> by ShortStickyRightShift,
        RoundingRightShift<KShort> by ShortRoundingRightShift,
        Addition<KShort>, Multiplication<KShort>,
        ShortBitRotate,
        Signed<KShort> {
        // Multiple interfaces define it, so we override explicitly
        override val sizeBits: KInt by KShort.Companion::SIZE_BITS

        override fun add(lhs: KShort, rhs: KShort): KShort = (lhs + rhs).toShort()
        override fun subtract(lhs: KShort, rhs: KShort): KShort = (lhs - rhs).toShort()

        override fun multiply(lhs: KShort, rhs: KShort): KShort = (lhs * rhs).toShort()
        override fun divide(lhs: KShort, rhs: KShort): KShort = (lhs / rhs).toShort()

        override fun isPositive(value: KShort): Boolean = value > 0
        override fun isNegative(value: KShort): Boolean = value < 0
        override fun negate(value: KShort): KShort = (-value).toShort()
        override fun absoluteValue(value: KShort): KShort = value.toInt().absoluteValue.toShort()
    }

    /**
     * Traits object for the [UShort][KUShort] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object UShort :
        BitsBased<KUShort, KShort> by UShortBitsBased,
        BitStore<KUShort> by UShortBitStore,
        StickyRightShift<KUShort> by UShortStickyRightShift,
        RoundingRightShift<KUShort> by UShortRoundingRightShift,
        Addition<KUShort>, Multiplication<KUShort>,
        UShortBitRotate {
        // Multiple interfaces define it, so we override explicitly
        override val sizeBits: KInt by KUShort.Companion::SIZE_BITS

        override fun add(lhs: KUShort, rhs: KUShort): KUShort = (lhs + rhs).toUShort()
        override fun subtract(lhs: KUShort, rhs: KUShort): KUShort = (lhs - rhs).toUShort()

        override fun multiply(lhs: KUShort, rhs: KUShort): KUShort = (lhs * rhs).toUShort()
        override fun divide(lhs: KUShort, rhs: KUShort): KUShort = (lhs / rhs).toUShort()
    }

    /**
     * Traits object for the [Int][KInt] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object Int :
        BitsBased<KInt, KInt> by IntBitsBased,
        BitStore<KInt> by IntBitStore,
        StickyRightShift<KInt> by IntStickyRightShift,
        RoundingRightShift<KInt> by IntRoundingRightShift,
        Addition<KInt>, Multiplication<KInt>,
        IntBitRotate,
        Signed<KInt> {
        // Multiple interfaces define it, so we override explicitly
        override val sizeBits: KInt by KInt.Companion::SIZE_BITS

        override fun add(lhs: KInt, rhs: KInt): KInt = lhs + rhs
        override fun subtract(lhs: KInt, rhs: KInt): KInt = lhs - rhs

        override fun multiply(lhs: KInt, rhs: KInt): KInt = lhs * rhs
        override fun divide(lhs: KInt, rhs: KInt): KInt = lhs / rhs

        override fun isPositive(value: KInt): Boolean = value > 0
        override fun isNegative(value: KInt): Boolean = value < 0
        override fun negate(value: KInt): KInt = -value
        override fun absoluteValue(value: KInt): KInt = value.absoluteValue
    }

    /**
     * Traits object for the [UInt][KUInt] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object UInt :
        BitsBased<KUInt, KInt> by UIntBitsBased,
        BitStore<KUInt> by UIntBitStore,
        StickyRightShift<KUInt> by UIntStickyRightShift,
        RoundingRightShift<KUInt> by UIntRoundingRightShift,
        Addition<KUInt>, Multiplication<KUInt>,
        UIntBitRotate {
        // Multiple interfaces define it, so we override explicitly
        override val sizeBits: KInt by KUInt.Companion::SIZE_BITS

        override fun add(lhs: KUInt, rhs: KUInt): KUInt = lhs + rhs
        override fun subtract(lhs: KUInt, rhs: KUInt): KUInt = lhs - rhs

        override fun multiply(lhs: KUInt, rhs: KUInt): KUInt = lhs * rhs
        override fun divide(lhs: KUInt, rhs: KUInt): KUInt = lhs / rhs
    }

    /**
     * Traits object for the [Long][KLong] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object Long :
        BitsBased<KLong, KLong> by LongBitsBased,
        BitStore<KLong> by LongBitStore,
        StickyRightShift<KLong> by LongStickyRightShift,
        RoundingRightShift<KLong> by LongRoundingRightShift,
        Addition<KLong>, Multiplication<KLong>,
        LongBitRotate,
        Signed<KLong> {
        // Multiple interfaces define it, so we override explicitly
        override val sizeBits: KInt by KLong.Companion::SIZE_BITS

        override fun add(lhs: KLong, rhs: KLong): KLong = lhs + rhs
        override fun subtract(lhs: KLong, rhs: KLong): KLong = lhs - rhs

        override fun multiply(lhs: KLong, rhs: KLong): KLong = lhs * rhs
        override fun divide(lhs: KLong, rhs: KLong): KLong = lhs / rhs

        override fun isPositive(value: KLong): Boolean = value > 0
        override fun isNegative(value: KLong): Boolean = value < 0
        override fun negate(value: KLong): KLong = -value
        override fun absoluteValue(value: KLong): KLong = value.absoluteValue
    }

    /**
     * Traits object for the [ULong][KULong] type.
     */
    @Suppress("detekt:TooManyFunctions")
    object ULong :
        BitsBased<KULong, KLong> by ULongBitsBased,
        BitStore<KULong> by ULongBitStore,
        StickyRightShift<KULong> by ULongStickyRightShift,
        RoundingRightShift<KULong> by ULongRoundingRightShift,
        Addition<KULong>, Multiplication<KULong>,
        ULongBitRotate {
        // Multiple interfaces define it, so we override explicitly
        override val sizeBits: KInt by KULong.Companion::SIZE_BITS

        override fun add(lhs: KULong, rhs: KULong): KULong = lhs + rhs
        override fun subtract(lhs: KULong, rhs: KULong): KULong = lhs - rhs

        override fun multiply(lhs: KULong, rhs: KULong): KULong = lhs * rhs
        override fun divide(lhs: KULong, rhs: KULong): KULong = lhs / rhs
    }

    @Suppress("detekt:TooManyFunctions")
    object Float :
        BitsBased<KFloat, KInt> by FloatBitsBased,
        Binary32Traits<KFloat>, FloatingPoint<KFloat>, Addition<KFloat>, Multiplication<KFloat>, Signed<KFloat> {
        // Multiple interfaces define it, so we override explicitly
        override val sizeBits: KInt by KFloat.Companion::SIZE_BITS

        override val positiveInfinity: KFloat by KFloat.Companion::POSITIVE_INFINITY
        override val negativeInfinity: KFloat by KFloat.Companion::NEGATIVE_INFINITY
        override val NaN: KFloat by KFloat.Companion::NaN

        override val zero: KFloat = 0.0f
        override val one: KFloat = 1.0f
        override fun isNaN(value: KFloat): Boolean = value.isNaN()
        override fun isFinite(value: KFloat): Boolean = value.isFinite()
        override fun isInfinite(value: KFloat): Boolean = value.isInfinite()

        override fun add(lhs: KFloat, rhs: KFloat): KFloat = lhs + rhs
        override fun subtract(lhs: KFloat, rhs: KFloat): KFloat = lhs - rhs

        override fun multiply(lhs: KFloat, rhs: KFloat): KFloat = lhs * rhs
        override fun divide(lhs: KFloat, rhs: KFloat): KFloat = lhs / rhs

        override fun isPositive(value: KFloat): Boolean = value > 0
        override fun isNegative(value: KFloat): Boolean = value < 0
        override fun negate(value: KFloat): KFloat = -value
        override fun absoluteValue(value: KFloat): KFloat = value.absoluteValue
    }

    @Suppress("detekt:TooManyFunctions")
    object Double :
        BitsBased<KDouble, KLong> by DoubleBitsBased,
        Binary64Traits<KDouble>, FloatingPoint<KDouble>, Addition<KDouble>, Multiplication<KDouble>, Signed<KDouble> {
        // Multiple interfaces define it, so we override explicitly
        override val sizeBits: KInt by KDouble.Companion::SIZE_BITS

        override val positiveInfinity: KDouble by KDouble.Companion::POSITIVE_INFINITY
        override val negativeInfinity: KDouble by KDouble.Companion::NEGATIVE_INFINITY
        override val NaN: KDouble by KDouble.Companion::NaN

        override val zero: KDouble = 0.0
        override val one: KDouble = 1.0
        override fun isNaN(value: KDouble): Boolean = value.isNaN()
        override fun isFinite(value: KDouble): Boolean = value.isFinite()
        override fun isInfinite(value: KDouble): Boolean = value.isInfinite()

        override fun add(lhs: KDouble, rhs: KDouble): KDouble = lhs + rhs
        override fun subtract(lhs: KDouble, rhs: KDouble): KDouble = lhs - rhs

        override fun multiply(lhs: KDouble, rhs: KDouble): KDouble = lhs * rhs
        override fun divide(lhs: KDouble, rhs: KDouble): KDouble = lhs / rhs

        override fun isPositive(value: KDouble): Boolean = value > 0
        override fun isNegative(value: KDouble): Boolean = value < 0
        override fun negate(value: KDouble): KDouble = -value
        override fun absoluteValue(value: KDouble): KDouble = value.absoluteValue
    }

    @Suppress("detekt:TooManyFunctions")
    object ByteArray : ArrayLike<KByteArray, KByte>, ByteArrayBitShift {
        override fun create(size: KInt, init: (KInt) -> KByte): KByteArray = KByteArray(size, init)
        override fun getAt(array: KByteArray, index: KInt): KByte = array[index]
        override fun getSize(array: KByteArray): KInt = array.size

        override fun any(array: KByteArray, predicate: (KByte) -> Boolean): Boolean = array.any(predicate)
        override fun all(array: KByteArray, predicate: (KByte) -> Boolean): Boolean = array.all(predicate)
        override fun none(array: KByteArray, predicate: (KByte) -> Boolean): Boolean = array.none(predicate)

        override fun <R> map(array: KByteArray, transform: (KByte) -> R): List<R> = array.map(transform)
        override fun <R> mapIndexed(array: KByteArray, transform: (KInt, KByte) -> R): List<R> = array.mapIndexed(transform)
        override fun <R> flatMap(array: KByteArray, transform: (KByte) -> Iterable<R>): List<R> = array.flatMap(transform)
        override fun <R> flatMapIndexed(array: KByteArray, transform: (KInt, KByte) -> Iterable<R>): List<R> = array.flatMapIndexed(transform)

        override fun forEach(array: KByteArray, action: (KByte) -> Unit) = array.forEach(action)
        override fun forEachIndexed(array: KByteArray, action: (KInt, KByte) -> Unit) = array.forEachIndexed(action)

        override fun indexOfFirst(array: KByteArray, predicate: (KByte) -> Boolean): KInt = array.indexOfFirst(predicate)
        override fun indexOfLast(array: KByteArray, predicate: (KByte) -> Boolean): KInt = array.indexOfLast(predicate)
    }

    @Suppress("detekt:TooManyFunctions")
    @OptIn(ExperimentalUnsignedTypes::class)
    object UByteArray : ArrayLike<KUByteArray, KUByte>, UByteArrayBitShift {
        override fun create(size: KInt, init: (KInt) -> KUByte): KUByteArray = KUByteArray(size, init)
        override fun getAt(array: KUByteArray, index: KInt): KUByte = array[index]
        override fun getSize(array: KUByteArray): KInt = array.size

        override fun any(array: KUByteArray, predicate: (KUByte) -> Boolean): Boolean = array.any(predicate)
        override fun all(array: KUByteArray, predicate: (KUByte) -> Boolean): Boolean = array.all(predicate)
        override fun none(array: KUByteArray, predicate: (KUByte) -> Boolean): Boolean = array.none(predicate)

        override fun <R> map(array: KUByteArray, transform: (KUByte) -> R): List<R> = array.map(transform)
        override fun <R> mapIndexed(array: KUByteArray, transform: (KInt, KUByte) -> R): List<R> = array.mapIndexed(transform)
        override fun <R> flatMap(array: KUByteArray, transform: (KUByte) -> Iterable<R>): List<R> = array.flatMap(transform)
        override fun <R> flatMapIndexed(array: KUByteArray, transform: (KInt, KUByte) -> Iterable<R>): List<R> = array.flatMapIndexed(transform)

        override fun forEach(array: KUByteArray, action: (KUByte) -> Unit) = array.forEach(action)
        override fun forEachIndexed(array: KUByteArray, action: (KInt, KUByte) -> Unit) = array.forEachIndexed(action)

        override fun indexOfFirst(array: KUByteArray, predicate: (KUByte) -> Boolean): KInt = array.indexOfFirst(predicate)
        override fun indexOfLast(array: KUByteArray, predicate: (KUByte) -> Boolean): KInt = array.indexOfLast(predicate)
    }

    @Suppress("detekt:TooManyFunctions")
    object ShortArray : ArrayLike<KShortArray, KShort>, ShortArrayBitShift {
        override fun create(size: KInt, init: (KInt) -> KShort): KShortArray = KShortArray(size, init)
        override fun getAt(array: KShortArray, index: KInt): KShort = array[index]
        override fun getSize(array: KShortArray): KInt = array.size

        override fun any(array: KShortArray, predicate: (KShort) -> Boolean): Boolean = array.any(predicate)
        override fun all(array: KShortArray, predicate: (KShort) -> Boolean): Boolean = array.all(predicate)
        override fun none(array: KShortArray, predicate: (KShort) -> Boolean): Boolean = array.none(predicate)

        override fun <R> map(array: KShortArray, transform: (KShort) -> R): List<R> = array.map(transform)
        override fun <R> mapIndexed(array: KShortArray, transform: (KInt, KShort) -> R): List<R> = array.mapIndexed(transform)
        override fun <R> flatMap(array: KShortArray, transform: (KShort) -> Iterable<R>): List<R> = array.flatMap(transform)
        override fun <R> flatMapIndexed(array: KShortArray, transform: (KInt, KShort) -> Iterable<R>): List<R> = array.flatMapIndexed(transform)

        override fun forEach(array: KShortArray, action: (KShort) -> Unit) = array.forEach(action)
        override fun forEachIndexed(array: KShortArray, action: (KInt, KShort) -> Unit) = array.forEachIndexed(action)

        override fun indexOfFirst(array: KShortArray, predicate: (KShort) -> Boolean): KInt = array.indexOfFirst(predicate)
        override fun indexOfLast(array: KShortArray, predicate: (KShort) -> Boolean): KInt = array.indexOfLast(predicate)
    }

    @Suppress("detekt:TooManyFunctions")
    @OptIn(ExperimentalUnsignedTypes::class)
    object UShortArray : ArrayLike<KUShortArray, KUShort>, UShortArrayBitShift {
        override fun create(size: KInt, init: (KInt) -> KUShort): KUShortArray = KUShortArray(size, init)
        override fun getAt(array: KUShortArray, index: KInt): KUShort = array[index]
        override fun getSize(array: KUShortArray): KInt = array.size

        override fun any(array: KUShortArray, predicate: (KUShort) -> Boolean): Boolean = array.any(predicate)
        override fun all(array: KUShortArray, predicate: (KUShort) -> Boolean): Boolean = array.all(predicate)
        override fun none(array: KUShortArray, predicate: (KUShort) -> Boolean): Boolean = array.none(predicate)

        override fun <R> map(array: KUShortArray, transform: (KUShort) -> R): List<R> = array.map(transform)
        override fun <R> mapIndexed(array: KUShortArray, transform: (KInt, KUShort) -> R): List<R> = array.mapIndexed(transform)
        override fun <R> flatMap(array: KUShortArray, transform: (KUShort) -> Iterable<R>): List<R> = array.flatMap(transform)
        override fun <R> flatMapIndexed(array: KUShortArray, transform: (KInt, KUShort) -> Iterable<R>): List<R> = array.flatMapIndexed(transform)

        override fun forEach(array: KUShortArray, action: (KUShort) -> Unit) = array.forEach(action)
        override fun forEachIndexed(array: KUShortArray, action: (KInt, KUShort) -> Unit) = array.forEachIndexed(action)

        override fun indexOfFirst(array: KUShortArray, predicate: (KUShort) -> Boolean): KInt = array.indexOfFirst(predicate)
        override fun indexOfLast(array: KUShortArray, predicate: (KUShort) -> Boolean): KInt = array.indexOfLast(predicate)
    }

    @Suppress("detekt:TooManyFunctions")
    object IntArray : ArrayLike<KIntArray, KInt>, IntArrayBitShift {
        override fun create(size: KInt, init: (KInt) -> KInt): KIntArray = KIntArray(size, init)
        override fun getAt(array: KIntArray, index: KInt): KInt = array[index]
        override fun getSize(array: KIntArray): KInt = array.size

        override fun any(array: KIntArray, predicate: (KInt) -> Boolean): Boolean = array.any(predicate)
        override fun all(array: KIntArray, predicate: (KInt) -> Boolean): Boolean = array.all(predicate)
        override fun none(array: KIntArray, predicate: (KInt) -> Boolean): Boolean = array.none(predicate)

        override fun <R> map(array: KIntArray, transform: (KInt) -> R): List<R> = array.map(transform)
        override fun <R> mapIndexed(array: KIntArray, transform: (KInt, KInt) -> R): List<R> = array.mapIndexed(transform)
        override fun <R> flatMap(array: KIntArray, transform: (KInt) -> Iterable<R>): List<R> = array.flatMap(transform)
        override fun <R> flatMapIndexed(array: KIntArray, transform: (KInt, KInt) -> Iterable<R>): List<R> = array.flatMapIndexed(transform)

        override fun forEach(array: KIntArray, action: (KInt) -> Unit) = array.forEach(action)
        override fun forEachIndexed(array: KIntArray, action: (KInt, KInt) -> Unit) = array.forEachIndexed(action)

        override fun indexOfFirst(array: KIntArray, predicate: (KInt) -> Boolean): KInt = array.indexOfFirst(predicate)
        override fun indexOfLast(array: KIntArray, predicate: (KInt) -> Boolean): KInt = array.indexOfLast(predicate)
    }

    @Suppress("detekt:TooManyFunctions")
    @OptIn(ExperimentalUnsignedTypes::class)
    object UIntArray : ArrayLike<KUIntArray, KUInt>, UIntArrayBitShift {
        override fun create(size: KInt, init: (KInt) -> KUInt): KUIntArray = KUIntArray(size, init)
        override fun getAt(array: KUIntArray, index: KInt): KUInt = array[index]
        override fun getSize(array: KUIntArray): KInt = array.size

        override fun any(array: KUIntArray, predicate: (KUInt) -> Boolean): Boolean = array.any(predicate)
        override fun all(array: KUIntArray, predicate: (KUInt) -> Boolean): Boolean = array.all(predicate)
        override fun none(array: KUIntArray, predicate: (KUInt) -> Boolean): Boolean = array.none(predicate)

        override fun <R> map(array: KUIntArray, transform: (KUInt) -> R): List<R> = array.map(transform)
        override fun <R> mapIndexed(array: KUIntArray, transform: (KInt, KUInt) -> R): List<R> = array.mapIndexed(transform)
        override fun <R> flatMap(array: KUIntArray, transform: (KUInt) -> Iterable<R>): List<R> = array.flatMap(transform)
        override fun <R> flatMapIndexed(array: KUIntArray, transform: (KInt, KUInt) -> Iterable<R>): List<R> = array.flatMapIndexed(transform)

        override fun forEach(array: KUIntArray, action: (KUInt) -> Unit) = array.forEach(action)
        override fun forEachIndexed(array: KUIntArray, action: (KInt, KUInt) -> Unit) = array.forEachIndexed(action)

        override fun indexOfFirst(array: KUIntArray, predicate: (KUInt) -> Boolean): KInt = array.indexOfFirst(predicate)
        override fun indexOfLast(array: KUIntArray, predicate: (KUInt) -> Boolean): KInt = array.indexOfLast(predicate)
    }

    @Suppress("detekt:TooManyFunctions")
    object LongArray : ArrayLike<KLongArray, KLong>, LongArrayBitShift {
        override fun create(size: KInt, init: (KInt) -> KLong): KLongArray = KLongArray(size, init)
        override fun getAt(array: KLongArray, index: KInt): KLong = array[index]
        override fun getSize(array: KLongArray): KInt = array.size

        override fun any(array: KLongArray, predicate: (KLong) -> Boolean): Boolean = array.any(predicate)
        override fun all(array: KLongArray, predicate: (KLong) -> Boolean): Boolean = array.all(predicate)
        override fun none(array: KLongArray, predicate: (KLong) -> Boolean): Boolean = array.none(predicate)

        override fun <R> map(array: KLongArray, transform: (KLong) -> R): List<R> = array.map(transform)
        override fun <R> mapIndexed(array: KLongArray, transform: (KInt, KLong) -> R): List<R> = array.mapIndexed(transform)
        override fun <R> flatMap(array: KLongArray, transform: (KLong) -> Iterable<R>): List<R> = array.flatMap(transform)
        override fun <R> flatMapIndexed(array: KLongArray, transform: (KInt, KLong) -> Iterable<R>): List<R> = array.flatMapIndexed(transform)

        override fun forEach(array: KLongArray, action: (KLong) -> Unit) = array.forEach(action)
        override fun forEachIndexed(array: KLongArray, action: (KInt, KLong) -> Unit) = array.forEachIndexed(action)

        override fun indexOfFirst(array: KLongArray, predicate: (KLong) -> Boolean): KInt = array.indexOfFirst(predicate)
        override fun indexOfLast(array: KLongArray, predicate: (KLong) -> Boolean): KInt = array.indexOfLast(predicate)
    }

    @Suppress("detekt:TooManyFunctions")
    @OptIn(ExperimentalUnsignedTypes::class)
    object ULongArray : ArrayLike<KULongArray, KULong>, ULongArrayBitShift {
        override fun create(size: KInt, init: (KInt) -> KULong): KULongArray = KULongArray(size, init)
        override fun getAt(array: KULongArray, index: KInt): KULong = array[index]
        override fun getSize(array: KULongArray): KInt = array.size

        override fun any(array: KULongArray, predicate: (KULong) -> Boolean): Boolean = array.any(predicate)
        override fun all(array: KULongArray, predicate: (KULong) -> Boolean): Boolean = array.all(predicate)
        override fun none(array: KULongArray, predicate: (KULong) -> Boolean): Boolean = array.none(predicate)

        override fun <R> map(array: KULongArray, transform: (KULong) -> R): List<R> = array.map(transform)
        override fun <R> mapIndexed(array: KULongArray, transform: (KInt, KULong) -> R): List<R> = array.mapIndexed(transform)
        override fun <R> flatMap(array: KULongArray, transform: (KULong) -> Iterable<R>): List<R> = array.flatMap(transform)
        override fun <R> flatMapIndexed(array: KULongArray, transform: (KInt, KULong) -> Iterable<R>): List<R> = array.flatMapIndexed(transform)

        override fun forEach(array: KULongArray, action: (KULong) -> Unit) = array.forEach(action)
        override fun forEachIndexed(array: KULongArray, action: (KInt, KULong) -> Unit) = array.forEachIndexed(action)

        override fun indexOfFirst(array: KULongArray, predicate: (KULong) -> Boolean): KInt = array.indexOfFirst(predicate)
        override fun indexOfLast(array: KULongArray, predicate: (KULong) -> Boolean): KInt = array.indexOfLast(predicate)
    }
}
