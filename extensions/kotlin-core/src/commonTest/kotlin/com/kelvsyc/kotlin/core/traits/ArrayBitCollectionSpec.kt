package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.intRange
import io.kotest.property.checkAll

@OptIn(ExperimentalStdlibApi::class)
class ArrayBitCollectionSpec : FunSpec() {
    class ByteArrayBitCollection(size: Int): ArrayBitCollection<Array<Byte>, Byte>(
        ArraySized.ofObjectArray(size, TypeTraits.Byte), arrayLike(), TypeTraits.Byte
    )

    init {
        context("Object array") {
            test("fromBits") {
                val arb = Arb.Companion.intRange(0 ..< Short.SIZE_BITS)
                checkAll(arb) {
                    val expected = TypeTraits.Short.fromBits(it).toUShort().toInt()

                    val bytes = ByteArrayBitCollection(Short.SIZE_BYTES).fromBits(it)
                    val rebuilt = (bytes[1].toUByte().toInt() shl Byte.SIZE_BITS) or bytes[0].toUByte().toInt()

                    rebuilt shouldBeEqual expected
                }
            }

            test("asBitSequence") {
                checkAll<Short> {
                    val bytes = TypeTraits.Short.asByteArray(it).toTypedArray()

                    val result = ByteArrayBitCollection(Short.SIZE_BYTES).asBitSequence(bytes)

                    result.toList() shouldBeEqual TypeTraits.Short.asBitSequence(it).toList()
                }
            }

            test("asByteArray") {
                checkAll<Short> {
                    val bytes = TypeTraits.Short.asByteArray(it).toTypedArray()

                    val result = ByteArrayBitCollection(Short.SIZE_BYTES).asByteArray(bytes)

                    result.toList() shouldBeEqual TypeTraits.Short.asByteArray(it).toList()
                }
            }

            test("getSetBits") {
                checkAll<Short> {
                    val bytes = TypeTraits.Short.asByteArray(it).toTypedArray()

                    val result = ByteArrayBitCollection(Short.SIZE_BYTES).getSetBits(bytes)

                    result shouldBeEqual TypeTraits.Short.getSetBits(it)
                }
            }

            test("countLeadingZeroBits") {
                checkAll<Short> {
                    val bytes = TypeTraits.Short.asByteArray(it).toTypedArray()

                    val result = ByteArrayBitCollection(Short.SIZE_BYTES).countLeadingZeroBits(bytes)

                    result shouldBeEqual it.countLeadingZeroBits()
                }
            }

            test("countTrailingZeroBits") {
                checkAll<Short> {
                    val bytes = TypeTraits.Short.asByteArray(it).toTypedArray()

                    val result = ByteArrayBitCollection(Short.SIZE_BYTES).countTrailingZeroBits(bytes)

                    result shouldBeEqual it.countTrailingZeroBits()
                }
            }
        }
    }
}
