@file:Suppress("detekt:TooManyFunctions")
package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.BitStore
import com.kelvsyc.kotlin.core.traits.BitsBased
import kotlin.reflect.KMutableProperty0
import kotlin.reflect.KProperty0

@JvmName("byteFlag")
fun flag(backingProperty: KProperty0<Byte>, off: Int) = bitfield(backingProperty, off, 1, TypeTraits.Byte) {
    it.toInt() != 0
}

@JvmName("mutableByteFlag")
fun flag(backingProperty: KMutableProperty0<Byte>, off: Int) =
    bitfield(backingProperty, off, 1, TypeTraits.Byte, Converter.of({it.toInt() != 0}, {if (it) 0x01 else 0x00}))

@JvmName("shortFlag")
fun flag(backingProperty: KProperty0<Short>, off: Int) = bitfield(backingProperty, off, 1, TypeTraits.Short) {
    it.toInt() != 0
}

@JvmName("mutableShortFlag")
fun flag(backingProperty: KMutableProperty0<Short>, off: Int) =
    bitfield(backingProperty, off, 1, TypeTraits.Short, Converter.of({it.toInt() != 0}, {if (it) 0x01 else 0x00}))

@JvmName("intFlag")
fun flag(backingProperty: KProperty0<Int>, off: Int) = bitfield(backingProperty, off, 1, TypeTraits.Int) {
    it != 0
}

@JvmName("mutableIntFlag")
fun flag(backingProperty: KMutableProperty0<Int>, off: Int) =
    bitfield(backingProperty, off, 1, TypeTraits.Int, Converter.of({it != 0}, {if (it) 0x01 else 0x00}))

@JvmName("longFlag")
fun flag(backingProperty: KProperty0<Long>, off: Int) = bitfield(backingProperty, off, 1, TypeTraits.Long) {
    it != 0L
}

@JvmName("mutableLongFlag")
fun flag(backingProperty: KMutableProperty0<Long>, off: Int) =
    bitfield(backingProperty, off, 1, TypeTraits.Long, Converter.of({it != 0L}, {if (it) 0x01 else 0x00}))

@JvmName("byteBitfield")
fun <S, T> bitfield(backingProperty: KProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Byte>, fn: (Byte) -> T) =
    object : AbstractBitFieldDelegate<S, T, Byte>(backingProperty, off, len) {
        override val bitstore: BitStore<Byte> = TypeTraits.Byte
        override val bitsBased: BitsBased<S, Byte> = bitsBased
        override fun convert(bits: Byte): T = fn(bits)
    }

@JvmName("byteBitfield")
fun bitfield(backingProperty: KProperty0<Byte>, off: Int, len: Int) = bitfield(backingProperty, off, len, TypeTraits.Byte, {it})

@JvmName("mutableByteBitfield")
fun <S, T> bitfield(backingProperty: KMutableProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Byte>, converter: Converter<Byte, T>) =
    object: AbstractMutableBitFieldDelegate<S, T, Byte>(backingProperty, off, len, converter) {
        override val bitstore: BitStore<Byte> = TypeTraits.Byte
        override val bitsBased: BitsBased<S, Byte> = bitsBased
    }

@JvmName("mutableByteBitfield")
fun bitfield(backingProperty: KMutableProperty0<Byte>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, TypeTraits.Byte, Converter.identity())

@JvmName("shortBitfield")
fun <S, T> bitfield(backingProperty: KProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Short>, fn: (Short) -> T) =
    object : AbstractBitFieldDelegate<S, T, Short>(backingProperty, off, len) {
        override val bitstore: BitStore<Short> = TypeTraits.Short
        override val bitsBased: BitsBased<S, Short> = bitsBased
        override fun convert(bits: Short): T = fn(bits)
    }

@JvmName("shortBitfield")
fun bitfield(backingProperty: KProperty0<Short>, off: Int, len: Int) = bitfield(backingProperty, off, len, TypeTraits.Short, {it})

@JvmName("mutableShortBitfield")
fun <S, T> bitfield(backingProperty: KMutableProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Short>, converter: Converter<Short, T>) =
    object : AbstractMutableBitFieldDelegate<S, T, Short>(backingProperty, off, len, converter) {
        override val bitstore: BitStore<Short> = TypeTraits.Short
        override val bitsBased: BitsBased<S, Short> = bitsBased
    }

@JvmName("mutableShortBitfield")
fun bitfield(backingProperty: KMutableProperty0<Short>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, TypeTraits.Short, Converter.identity())

@JvmName("intBitfield")
fun <S, T> bitfield(backingProperty: KProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Int>, fn: (Int) -> T) =
    object : AbstractBitFieldDelegate<S, T, Int>(backingProperty, off, len) {
        override val bitstore: BitStore<Int> = TypeTraits.Int
        override val bitsBased: BitsBased<S, Int> = bitsBased
        override fun convert(bits: Int): T = fn(bits)
    }

@JvmName("intBitfield")
fun bitfield(backingProperty: KProperty0<Int>, off: Int, len: Int) = bitfield(backingProperty, off, len, TypeTraits.Int, {it})

@JvmName("mutableIntBitfield")
fun <S, T> bitfield(backingProperty: KMutableProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Int>, converter: Converter<Int, T>) =
    object: AbstractMutableBitFieldDelegate<S, T, Int>(backingProperty, off, len, converter) {
        override val bitstore: BitStore<Int> = TypeTraits.Int
        override val bitsBased: BitsBased<S, Int> = bitsBased
    }

@JvmName("mutableIntBitfield")
fun bitfield(backingProperty: KMutableProperty0<Int>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, TypeTraits.Int, Converter.identity())

@JvmName("longBitfield")
fun <S, T> bitfield(backingProperty: KProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Long>, fn: (Long) -> T) =
    object : AbstractBitFieldDelegate<S, T, Long>(backingProperty, off, len) {
        override val bitstore: BitStore<Long> = TypeTraits.Long
        override val bitsBased: BitsBased<S, Long> = bitsBased
        override fun convert(bits: Long): T = fn(bits)
    }

@JvmName("longBitfield")
fun bitfield(backingProperty: KProperty0<Long>, off: Int, len: Int) = bitfield(backingProperty, off, len, TypeTraits.Long, {it})

@JvmName("mutableLongBitfield")
fun <S, T> bitfield(backingProperty: KMutableProperty0<S>, off: Int, len: Int, bitsBased: BitsBased<S, Long>, converter: Converter<Long, T>) =
    object: AbstractMutableBitFieldDelegate<S, T, Long>(backingProperty, off, len, converter) {
        override val bitstore: BitStore<Long> = TypeTraits.Long
        override val bitsBased: BitsBased<S, Long> = bitsBased
    }

@JvmName("mutableLongBitfield")
fun bitfield(backingProperty: KMutableProperty0<Long>, off: Int, len: Int) =
    bitfield(backingProperty, off, len, TypeTraits.Long, Converter.identity())
