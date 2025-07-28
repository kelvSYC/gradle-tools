package com.kelvsyc.kotlin.core.traits

import com.kelvsyc.kotlin.core.TypeTraits
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.Arb
import io.kotest.property.arbitrary.negativeInt
import io.kotest.property.arbitrary.nonNegativeInt
import io.kotest.property.checkAll

@OptIn(ExperimentalUnsignedTypes::class)
class ArraySizedSpec : FunSpec() {
    init {
        context("Object Array") {
            test("Normal") {
                val sizeArb = Arb.nonNegativeInt()
                checkAll(sizeArb) {
                    val traits = arraySized(it, TypeTraits.Int)

                    traits.sizeBits shouldBeEqual Int.SIZE_BITS * it
                    traits.sizeBytes shouldBeEqual Int.SIZE_BYTES * it
                }
            }
            test("Negative size") {
                val sizeArb = Arb.negativeInt()
                checkAll(sizeArb) {
                    shouldThrow<IllegalArgumentException> {
                        arraySized(it, TypeTraits.Int)
                    }
                }
            }
        }

        context("ByteArray") {
            test("Normal") {
                val sizeArb = Arb.nonNegativeInt()
                checkAll(sizeArb) {
                    val traits = byteArraySized(it)

                    traits.sizeBits shouldBeEqual Byte.SIZE_BITS * it
                    traits.sizeBytes shouldBeEqual Byte.SIZE_BYTES * it
                }
            }
            test("Negative size") {
                val sizeArb = Arb.negativeInt()
                checkAll(sizeArb) {
                    shouldThrow<IllegalArgumentException> {
                        byteArraySized(it)
                    }
                }
            }
        }

        context("UByteArray") {
            test("Normal") {
                val sizeArb = Arb.nonNegativeInt()
                checkAll(sizeArb) {
                    val traits = uByteArraySized(it)

                    traits.sizeBits shouldBeEqual UByte.SIZE_BITS * it
                    traits.sizeBytes shouldBeEqual UByte.SIZE_BYTES * it
                }
            }
            test("Negative size") {
                val sizeArb = Arb.negativeInt()
                checkAll(sizeArb) {
                    shouldThrow<IllegalArgumentException> {
                        uByteArraySized(it)
                    }
                }
            }
        }

        context("ShortArray") {
            test("Normal") {
                val sizeArb = Arb.nonNegativeInt()
                checkAll(sizeArb) {
                    val traits = shortArraySized(it)

                    traits.sizeBits shouldBeEqual Short.SIZE_BITS * it
                    traits.sizeBytes shouldBeEqual Short.SIZE_BYTES * it
                }
            }
            test("Negative size") {
                val sizeArb = Arb.negativeInt()
                checkAll(sizeArb) {
                    shouldThrow<IllegalArgumentException> {
                        shortArraySized(it)
                    }
                }
            }
        }

        context("UShortArray") {
            test("Normal") {
                val sizeArb = Arb.nonNegativeInt()
                checkAll(sizeArb) {
                    val traits = uShortArraySized(it)

                    traits.sizeBits shouldBeEqual UShort.SIZE_BITS * it
                    traits.sizeBytes shouldBeEqual UShort.SIZE_BYTES * it
                }
            }
            test("Negative size") {
                val sizeArb = Arb.negativeInt()
                checkAll(sizeArb) {
                    shouldThrow<IllegalArgumentException> {
                        uShortArraySized(it)
                    }
                }
            }
        }

        context("IntArray") {
            test("Normal") {
                val sizeArb = Arb.nonNegativeInt()
                checkAll(sizeArb) {
                    val traits = intArraySized(it)

                    traits.sizeBits shouldBeEqual Int.SIZE_BITS * it
                    traits.sizeBytes shouldBeEqual Int.SIZE_BYTES * it
                }
            }
            test("Negative size") {
                val sizeArb = Arb.negativeInt()
                checkAll(sizeArb) {
                    shouldThrow<IllegalArgumentException> {
                        intArraySized(it)
                    }
                }
            }
        }

        context("UIntArray") {
            test("Normal") {
                val sizeArb = Arb.nonNegativeInt()
                checkAll(sizeArb) {
                    val traits = uIntArraySized(it)

                    traits.sizeBits shouldBeEqual UInt.SIZE_BITS * it
                    traits.sizeBytes shouldBeEqual UInt.SIZE_BYTES * it
                }
            }
            test("Negative size") {
                val sizeArb = Arb.negativeInt()
                checkAll(sizeArb) {
                    shouldThrow<IllegalArgumentException> {
                        uIntArraySized(it)
                    }
                }
            }
        }

        context("LongArray") {
            test("Normal") {
                val sizeArb = Arb.nonNegativeInt()
                checkAll(sizeArb) {
                    val traits = longArraySized(it)

                    traits.sizeBits shouldBeEqual Long.SIZE_BITS * it
                    traits.sizeBytes shouldBeEqual Long.SIZE_BYTES * it
                }
            }
            test("Negative size") {
                val sizeArb = Arb.negativeInt()
                checkAll(sizeArb) {
                    shouldThrow<IllegalArgumentException> {
                        longArraySized(it)
                    }
                }
            }
        }

        context("ULongArray") {
            test("Normal") {
                val sizeArb = Arb.nonNegativeInt()
                checkAll(sizeArb) {
                    val traits = uLongArraySized(it)

                    traits.sizeBits shouldBeEqual ULong.SIZE_BITS * it
                    traits.sizeBytes shouldBeEqual ULong.SIZE_BYTES * it
                }
            }
            test("Negative size") {
                val sizeArb = Arb.negativeInt()
                checkAll(sizeArb) {
                    shouldThrow<IllegalArgumentException> {
                        uLongArraySized(it)
                    }
                }
            }
        }

        context("FloatArray") {
            test("Normal") {
                val sizeArb = Arb.nonNegativeInt()
                checkAll(sizeArb) {
                    val traits = floatArraySized(it)

                    traits.sizeBits shouldBeEqual Float.SIZE_BITS * it
                    traits.sizeBytes shouldBeEqual Float.SIZE_BYTES * it
                }
            }
            test("Negative size") {
                val sizeArb = Arb.negativeInt()
                checkAll(sizeArb) {
                    shouldThrow<IllegalArgumentException> {
                        floatArraySized(it)
                    }
                }
            }
        }

        context("DoubleArray") {
            test("Normal") {
                val sizeArb = Arb.nonNegativeInt()
                checkAll(sizeArb) {
                    val traits = doubleArraySized(it)

                    traits.sizeBits shouldBeEqual Double.SIZE_BITS * it
                    traits.sizeBytes shouldBeEqual Double.SIZE_BYTES * it
                }
            }
            test("Negative size") {
                val sizeArb = Arb.negativeInt()
                checkAll(sizeArb) {
                    shouldThrow<IllegalArgumentException> {
                        doubleArraySized(it)
                    }
                }
            }
        }
    }
}
