package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.traits.ArrayLike
import com.kelvsyc.kotlin.core.traits.arrayLike
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import kotlin.experimental.and
import kotlin.experimental.inv
import kotlin.experimental.or
import kotlin.experimental.xor

class AbstractArrayBitwiseSpec : FunSpec() {
    class ByteArrayBitwise(size: Int? = null) : AbstractArrayBitwise<Array<Byte>, Byte>(size) {
        override val traits: ArrayLike<Array<Byte>, Byte> = arrayLike()
        override val base: Bitwise<Byte> = TypeTraits.Byte
        override val zero: Byte = 0
    }

    init {
        test("same size and - unsized") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Int.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise().and(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual lhsBytes.size
                rebuilt shouldBeEqual (lhs and rhs)
            }
        }

        test("different size and - unsized") {
            checkAll<Int, Short> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Short.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise().and(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual lhsBytes.size
                rebuilt shouldBeEqual (lhs and rhs.toUShort().toInt())
            }
        }

        test("same size and - size extended") {
            checkAll<Short, Short> { lhs, rhs ->
                val lhsBytes = TypeTraits.Short.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Short.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise(Int.SIZE_BYTES).and(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual Int.SIZE_BYTES
                rebuilt shouldBeEqual (lhs and rhs).toUShort().toInt()
            }
        }

        test("different size and - size extended") {
            checkAll<Short, Byte> { lhs, rhs ->
                val lhsBytes = TypeTraits.Short.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Byte.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise(Int.SIZE_BYTES).and(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual Int.SIZE_BYTES
                rebuilt shouldBeEqual (lhs.toUShort() and rhs.toUByte().toUShort()).toUInt().toInt()
            }
        }

        test("different size and - size truncated") {
            checkAll<Short, Byte> { lhs, rhs ->
                val lhsBytes = TypeTraits.Short.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Byte.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise(Byte.SIZE_BYTES).and(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual Byte.SIZE_BYTES
                rebuilt shouldBeEqual (lhs.toUShort() and rhs.toUByte().toUShort()).toUByte().toInt()
            }
        }

        test("same size or - unsized") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Int.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise().or(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual lhsBytes.size
                rebuilt shouldBeEqual (lhs or rhs)
            }
        }

        test("different size or - unsized") {
            checkAll<Int, Short> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Short.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise().or(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual lhsBytes.size
                rebuilt shouldBeEqual (lhs or rhs.toUShort().toInt())
            }
        }

        test("same size or - size extended") {
            checkAll<Short, Short> { lhs, rhs ->
                val lhsBytes = TypeTraits.Short.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Short.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise(Int.SIZE_BYTES).or(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual Int.SIZE_BYTES
                rebuilt shouldBeEqual (lhs or rhs).toUShort().toInt()
            }
        }

        test("different size or - size extended") {
            checkAll<Short, Byte> { lhs, rhs ->
                val lhsBytes = TypeTraits.Short.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Byte.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise(Int.SIZE_BYTES).or(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual Int.SIZE_BYTES
                rebuilt shouldBeEqual (lhs.toUShort() or rhs.toUByte().toUShort()).toUInt().toInt()
            }
        }

        test("different size or - size truncated") {
            checkAll<Short, Byte> { lhs, rhs ->
                val lhsBytes = TypeTraits.Short.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Byte.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise(Byte.SIZE_BYTES).or(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual Byte.SIZE_BYTES
                rebuilt shouldBeEqual (lhs.toUShort() or rhs.toUByte().toUShort()).toUByte().toInt()
            }
        }

        test("same size xor - unsized") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Int.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise().xor(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual lhsBytes.size
                rebuilt shouldBeEqual (lhs xor rhs)
            }
        }

        test("different size xor - unsized") {
            checkAll<Int, Short> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Short.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise().xor(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual lhsBytes.size
                rebuilt shouldBeEqual (lhs xor rhs.toUShort().toInt())
            }
        }

        test("same size xor - size extended") {
            checkAll<Short, Short> { lhs, rhs ->
                val lhsBytes = TypeTraits.Short.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Short.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise(Int.SIZE_BYTES).xor(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual Int.SIZE_BYTES
                rebuilt shouldBeEqual (lhs xor rhs).toUShort().toInt()
            }
        }

        test("different size xor - size extended") {
            checkAll<Short, Byte> { lhs, rhs ->
                val lhsBytes = TypeTraits.Short.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Byte.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise(Int.SIZE_BYTES).xor(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual Int.SIZE_BYTES
                rebuilt shouldBeEqual (lhs.toUShort() xor rhs.toUByte().toUShort()).toUInt().toInt()
            }
        }

        test("different size xor - size truncated") {
            checkAll<Short, Byte> { lhs, rhs ->
                val lhsBytes = TypeTraits.Short.asByteArray(lhs).toTypedArray()
                val rhsBytes = TypeTraits.Byte.asByteArray(rhs).toTypedArray()

                val result = ByteArrayBitwise(Byte.SIZE_BYTES).xor(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual Byte.SIZE_BYTES
                rebuilt shouldBeEqual (lhs.toUShort() xor rhs.toUByte().toUShort()).toUByte().toInt()
            }
        }

        test("inv - unsized") {
            checkAll<Int> { value ->
                val bytes = TypeTraits.Int.asByteArray(value).toTypedArray()

                val result = ByteArrayBitwise().inv(bytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual bytes.size
                rebuilt shouldBeEqual value.inv()
            }
        }

        test("inv - size extended") {
            checkAll<Short> { value ->
                val bytes = TypeTraits.Short.asByteArray(value).toTypedArray()

                val result = ByteArrayBitwise(Int.SIZE_BYTES).inv(bytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual Int.SIZE_BYTES
                rebuilt shouldBeEqual value.inv().toUShort().toInt()
            }
        }

        test("inv - size truncated") {
            checkAll<Short> { value ->
                val bytes = TypeTraits.Short.asByteArray(value).toTypedArray()

                val result = ByteArrayBitwise(Byte.SIZE_BYTES).inv(bytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                result.size shouldBeEqual Byte.SIZE_BYTES
                rebuilt shouldBeEqual value.inv().toUByte().toInt()
            }
        }
    }
}
