package com.kelvsyc.kotlin.core

import com.kelvsyc.internal.kotlin.core.traits.ByteArrayBitRotate
import com.kelvsyc.internal.kotlin.core.traits.ByteArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.ByteBitRotate
import com.kelvsyc.internal.kotlin.core.traits.ByteBitStore
import com.kelvsyc.internal.kotlin.core.traits.ByteBitStoreConstants
import com.kelvsyc.internal.kotlin.core.traits.ByteBitsBased
import com.kelvsyc.internal.kotlin.core.traits.ByteIntegerArithmetic
import com.kelvsyc.internal.kotlin.core.traits.ByteIntegralConstants
import com.kelvsyc.internal.kotlin.core.traits.ByteRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.ByteSigned
import com.kelvsyc.internal.kotlin.core.traits.ByteSized
import com.kelvsyc.internal.kotlin.core.traits.ByteStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.DoubleBitsBased
import com.kelvsyc.internal.kotlin.core.traits.DoubleFloatingPointArithmetic
import com.kelvsyc.internal.kotlin.core.traits.DoubleTraits
import com.kelvsyc.internal.kotlin.core.traits.FloatBitsBased
import com.kelvsyc.internal.kotlin.core.traits.FloatFloatingPointArithmetic
import com.kelvsyc.internal.kotlin.core.traits.FloatTraits
import com.kelvsyc.internal.kotlin.core.traits.IntArrayBitRotate
import com.kelvsyc.internal.kotlin.core.traits.IntArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.IntBitRotate
import com.kelvsyc.internal.kotlin.core.traits.IntBitStore
import com.kelvsyc.internal.kotlin.core.traits.IntBitStoreConstants
import com.kelvsyc.internal.kotlin.core.traits.IntBitsBased
import com.kelvsyc.internal.kotlin.core.traits.IntIntegerArithmetic
import com.kelvsyc.internal.kotlin.core.traits.IntIntegralConstants
import com.kelvsyc.internal.kotlin.core.traits.IntRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.IntSigned
import com.kelvsyc.internal.kotlin.core.traits.IntSized
import com.kelvsyc.internal.kotlin.core.traits.IntStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.LongArrayBitRotate
import com.kelvsyc.internal.kotlin.core.traits.LongArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.LongBitRotate
import com.kelvsyc.internal.kotlin.core.traits.LongBitStore
import com.kelvsyc.internal.kotlin.core.traits.LongBitStoreConstants
import com.kelvsyc.internal.kotlin.core.traits.LongBitsBased
import com.kelvsyc.internal.kotlin.core.traits.LongIntegerArithmetic
import com.kelvsyc.internal.kotlin.core.traits.LongIntegralConstants
import com.kelvsyc.internal.kotlin.core.traits.LongRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.LongSigned
import com.kelvsyc.internal.kotlin.core.traits.LongSized
import com.kelvsyc.internal.kotlin.core.traits.LongStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.ShortArrayBitRotate
import com.kelvsyc.internal.kotlin.core.traits.ShortArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.ShortBitRotate
import com.kelvsyc.internal.kotlin.core.traits.ShortBitStore
import com.kelvsyc.internal.kotlin.core.traits.ShortBitStoreConstants
import com.kelvsyc.internal.kotlin.core.traits.ShortBitsBased
import com.kelvsyc.internal.kotlin.core.traits.ShortIntegerArithmetic
import com.kelvsyc.internal.kotlin.core.traits.ShortIntegralConstants
import com.kelvsyc.internal.kotlin.core.traits.ShortRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.ShortSigned
import com.kelvsyc.internal.kotlin.core.traits.ShortSized
import com.kelvsyc.internal.kotlin.core.traits.ShortStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.UByteArrayBitRotate
import com.kelvsyc.internal.kotlin.core.traits.UByteArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.UByteBitRotate
import com.kelvsyc.internal.kotlin.core.traits.UByteBitStore
import com.kelvsyc.internal.kotlin.core.traits.UByteBitStoreConstants
import com.kelvsyc.internal.kotlin.core.traits.UByteBitsBased
import com.kelvsyc.internal.kotlin.core.traits.UByteIntegerArithmetic
import com.kelvsyc.internal.kotlin.core.traits.UByteIntegralConstants
import com.kelvsyc.internal.kotlin.core.traits.UByteRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.UByteSized
import com.kelvsyc.internal.kotlin.core.traits.UByteStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.UIntArrayBitRotate
import com.kelvsyc.internal.kotlin.core.traits.UIntArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.UIntBitRotate
import com.kelvsyc.internal.kotlin.core.traits.UIntBitStore
import com.kelvsyc.internal.kotlin.core.traits.UIntBitStoreConstants
import com.kelvsyc.internal.kotlin.core.traits.UIntBitsBased
import com.kelvsyc.internal.kotlin.core.traits.UIntIntegerArithmetic
import com.kelvsyc.internal.kotlin.core.traits.UIntIntegralConstants
import com.kelvsyc.internal.kotlin.core.traits.UIntRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.UIntSized
import com.kelvsyc.internal.kotlin.core.traits.UIntStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.ULongArrayBitRotate
import com.kelvsyc.internal.kotlin.core.traits.ULongArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.ULongBitRotate
import com.kelvsyc.internal.kotlin.core.traits.ULongBitStore
import com.kelvsyc.internal.kotlin.core.traits.ULongBitStoreConstants
import com.kelvsyc.internal.kotlin.core.traits.ULongBitsBased
import com.kelvsyc.internal.kotlin.core.traits.ULongIntegerArithmetic
import com.kelvsyc.internal.kotlin.core.traits.ULongIntegralConstants
import com.kelvsyc.internal.kotlin.core.traits.ULongRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.ULongSized
import com.kelvsyc.internal.kotlin.core.traits.ULongStickyRightShift
import com.kelvsyc.internal.kotlin.core.traits.UShortArrayBitRotate
import com.kelvsyc.internal.kotlin.core.traits.UShortArrayBitShift
import com.kelvsyc.internal.kotlin.core.traits.UShortBitRotate
import com.kelvsyc.internal.kotlin.core.traits.UShortBitStore
import com.kelvsyc.internal.kotlin.core.traits.UShortBitStoreConstants
import com.kelvsyc.internal.kotlin.core.traits.UShortBitsBased
import com.kelvsyc.internal.kotlin.core.traits.UShortIntegerArithmetic
import com.kelvsyc.internal.kotlin.core.traits.UShortIntegralConstants
import com.kelvsyc.internal.kotlin.core.traits.UShortRoundingRightShift
import com.kelvsyc.internal.kotlin.core.traits.UShortSized
import com.kelvsyc.internal.kotlin.core.traits.UShortStickyRightShift
import com.kelvsyc.kotlin.core.traits.ArrayLike
import com.kelvsyc.kotlin.core.traits.Binary32Traits
import com.kelvsyc.kotlin.core.traits.Binary64Traits
import com.kelvsyc.kotlin.core.traits.BitShift
import com.kelvsyc.kotlin.core.traits.BitStore
import com.kelvsyc.kotlin.core.traits.BitStoreConstants
import com.kelvsyc.kotlin.core.traits.BitsBased
import com.kelvsyc.kotlin.core.traits.FloatingPointArithmetic
import com.kelvsyc.kotlin.core.traits.IntegerArithmetic
import com.kelvsyc.kotlin.core.traits.IntegralConstants
import com.kelvsyc.kotlin.core.traits.RoundingRightShift
import com.kelvsyc.kotlin.core.traits.Signed
import com.kelvsyc.kotlin.core.traits.Sized
import com.kelvsyc.kotlin.core.traits.StickyRightShift
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
    object Byte :
        Sized by ByteSized,
        IntegralConstants<KByte> by ByteIntegralConstants,
        BitStoreConstants<KByte> by ByteBitStoreConstants,
        BitsBased<KByte, KByte> by ByteBitsBased,
        BitStore<KByte> by ByteBitStore,
        StickyRightShift<KByte> by ByteStickyRightShift,
        RoundingRightShift<KByte> by ByteRoundingRightShift,
        IntegerArithmetic<KByte> by ByteIntegerArithmetic,
        ByteBitRotate,
        Signed<KByte> by ByteSigned

    /**
     * Traits object for the [UByte][KUByte] type.
     */
    object UByte :
        Sized by UByteSized,
        IntegralConstants<KUByte> by UByteIntegralConstants,
        BitStoreConstants<KUByte> by UByteBitStoreConstants,
        BitsBased<KUByte, KByte> by UByteBitsBased,
        BitStore<KUByte> by UByteBitStore,
        StickyRightShift<KUByte> by UByteStickyRightShift,
        RoundingRightShift<KUByte> by UByteRoundingRightShift,
        IntegerArithmetic<KUByte> by UByteIntegerArithmetic,
        UByteBitRotate

    /**
     * Traits object for the [Short][KShort] type.
     */
    object Short :
        Sized by ShortSized,
        IntegralConstants<KShort> by ShortIntegralConstants,
        BitStoreConstants<KShort> by ShortBitStoreConstants,
        BitsBased<KShort, KShort> by ShortBitsBased,
        BitStore<KShort> by ShortBitStore,
        StickyRightShift<KShort> by ShortStickyRightShift,
        RoundingRightShift<KShort> by ShortRoundingRightShift,
        IntegerArithmetic<KShort> by ShortIntegerArithmetic,
        ShortBitRotate,
        Signed<KShort> by ShortSigned

    /**
     * Traits object for the [UShort][KUShort] type.
     */
    object UShort :
        Sized by UShortSized,
        IntegralConstants<KUShort> by UShortIntegralConstants,
        BitStoreConstants<KUShort> by UShortBitStoreConstants,
        BitsBased<KUShort, KShort> by UShortBitsBased,
        BitStore<KUShort> by UShortBitStore,
        StickyRightShift<KUShort> by UShortStickyRightShift,
        RoundingRightShift<KUShort> by UShortRoundingRightShift,
        IntegerArithmetic<KUShort> by UShortIntegerArithmetic,
        UShortBitRotate

    /**
     * Traits object for the [Int][KInt] type.
     */
    object Int :
        Sized by IntSized,
        IntegralConstants<KInt> by IntIntegralConstants,
        BitStoreConstants<KInt> by IntBitStoreConstants,
        BitsBased<KInt, KInt> by IntBitsBased,
        BitStore<KInt> by IntBitStore,
        StickyRightShift<KInt> by IntStickyRightShift,
        RoundingRightShift<KInt> by IntRoundingRightShift,
        IntegerArithmetic<KInt> by IntIntegerArithmetic,
        IntBitRotate,
        Signed<KInt> by IntSigned

    /**
     * Traits object for the [UInt][KUInt] type.
     */
    object UInt :
        Sized by UIntSized,
        IntegralConstants<KUInt> by UIntIntegralConstants,
        BitStoreConstants<KUInt> by UIntBitStoreConstants,
        BitsBased<KUInt, KInt> by UIntBitsBased,
        BitStore<KUInt> by UIntBitStore,
        StickyRightShift<KUInt> by UIntStickyRightShift,
        RoundingRightShift<KUInt> by UIntRoundingRightShift,
        IntegerArithmetic<KUInt> by UIntIntegerArithmetic,
        UIntBitRotate

    /**
     * Traits object for the [Long][KLong] type.
     */
    object Long :
        Sized by LongSized,
        IntegralConstants<KLong> by LongIntegralConstants,
        BitStoreConstants<KLong> by LongBitStoreConstants,
        BitsBased<KLong, KLong> by LongBitsBased,
        BitStore<KLong> by LongBitStore,
        StickyRightShift<KLong> by LongStickyRightShift,
        RoundingRightShift<KLong> by LongRoundingRightShift,
        IntegerArithmetic<KLong> by LongIntegerArithmetic,
        LongBitRotate,
        Signed<KLong> by LongSigned

    /**
     * Traits object for the [ULong][KULong] type.
     */
    object ULong :
        Sized by ULongSized,
        IntegralConstants<KULong> by ULongIntegralConstants,
        BitStoreConstants<KULong> by ULongBitStoreConstants,
        BitsBased<KULong, KLong> by ULongBitsBased,
        BitStore<KULong> by ULongBitStore,
        StickyRightShift<KULong> by ULongStickyRightShift,
        RoundingRightShift<KULong> by ULongRoundingRightShift,
        IntegerArithmetic<KULong> by ULongIntegerArithmetic,
        ULongBitRotate

    /**
     * Traits object for the [Float][KFloat] type.
     */
    object Float : Binary32Traits<KFloat> by FloatTraits,
        BitsBased<KFloat, KInt> by FloatBitsBased,
        FloatingPointArithmetic<KFloat> by FloatFloatingPointArithmetic

    /**
     * Traits object for the [Double][KDouble] type.
     */
    object Double : Binary64Traits<KDouble> by DoubleTraits,
        BitsBased<KDouble, KLong> by DoubleBitsBased,
        FloatingPointArithmetic<KDouble> by DoubleFloatingPointArithmetic

    @Suppress("detekt:TooManyFunctions")
    object ByteArray : ArrayLike<KByteArray, KByte>, BitShift<KByteArray> by ByteArrayBitShift, ByteArrayBitRotate {
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
    object UByteArray : ArrayLike<KUByteArray, KUByte>, BitShift<KUByteArray> by UByteArrayBitShift, UByteArrayBitRotate {
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
    object ShortArray : ArrayLike<KShortArray, KShort>, BitShift<KShortArray> by ShortArrayBitShift, ShortArrayBitRotate {
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
    object UShortArray : ArrayLike<KUShortArray, KUShort>, BitShift<KUShortArray> by UShortArrayBitShift, UShortArrayBitRotate {
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
    object IntArray : ArrayLike<KIntArray, KInt>, BitShift<KIntArray> by IntArrayBitShift, IntArrayBitRotate {
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
    object UIntArray : ArrayLike<KUIntArray, KUInt>, BitShift<KUIntArray> by UIntArrayBitShift, UIntArrayBitRotate {
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
    object LongArray : ArrayLike<KLongArray, KLong>, BitShift<KLongArray> by LongArrayBitShift, LongArrayBitRotate {
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
    object ULongArray : ArrayLike<KULongArray, KULong>, BitShift<KULongArray> by ULongArrayBitShift, ULongArrayBitRotate {
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
