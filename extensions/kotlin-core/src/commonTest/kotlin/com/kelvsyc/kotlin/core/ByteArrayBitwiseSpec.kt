package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll

@OptIn(ExperimentalStdlibApi::class)
class ByteArrayBitwiseSpec : FunSpec() {
    init {
        test("same size and") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs)
                val rhsBytes = TypeTraits.Int.asByteArray(rhs)

                val result = ByteArrayBitwise.and(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs and rhs)
            }
        }

        test("different size and") {
            checkAll<Int, Short> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs)
                val rhsBytes = TypeTraits.Short.asByteArray(rhs)

                val result = ByteArrayBitwise.and(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs and rhs.toUShort().toInt())
            }
        }

        test("same size or") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs)
                val rhsBytes = TypeTraits.Int.asByteArray(rhs)

                val result = ByteArrayBitwise.or(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs or rhs)
            }
        }

        test("different size or") {
            checkAll<Int, Short> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs)
                val rhsBytes = TypeTraits.Short.asByteArray(rhs)

                val result = ByteArrayBitwise.or(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs or rhs.toUShort().toInt())
            }
        }

        test("same size xor") {
            checkAll<Int, Int> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs)
                val rhsBytes = TypeTraits.Int.asByteArray(rhs)

                val result = ByteArrayBitwise.xor(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs xor rhs)
            }
        }

        test("different size xor") {
            checkAll<Int, Short> { lhs, rhs ->
                val lhsBytes = TypeTraits.Int.asByteArray(lhs)
                val rhsBytes = TypeTraits.Short.asByteArray(rhs)

                val result = ByteArrayBitwise.xor(lhsBytes, rhsBytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual (lhs xor rhs.toUShort().toInt())
            }
        }

        test("inv") {
            checkAll<Int> { value ->
                val bytes = TypeTraits.Int.asByteArray(value)

                val result = ByteArrayBitwise.inv(bytes)
                val rebuilt = result.foldIndexed(0) { index, acc, b ->
                    acc or ((b.toInt() and 0xFF) shl (index * Byte.SIZE_BITS))
                }

                rebuilt shouldBeEqual value.inv()
            }
        }
    }
}
