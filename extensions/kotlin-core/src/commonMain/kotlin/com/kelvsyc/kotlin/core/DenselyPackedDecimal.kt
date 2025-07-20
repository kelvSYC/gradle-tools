package com.kelvsyc.kotlin.core

import com.kelvsyc.kotlin.core.DenselyPackedDecimal.Companion.of


/**
 * Class representing a densely packed decimal (DPD) group.
 *
 * DPD is a binary-coded decimal format, where 3 decimal digits are packed into 10 bits. This class uses a [Short] to
 * store them, as the smallest Kotlin type that can hold 10 bits. All unused bits (such as the upper six bits in this
 * case) are ignored.
 */
data class DenselyPackedDecimal(val bits: Short) {
    companion object {
        private const val B3_FLAG = 0x0008
        private const val B12_FLAG = 0x0006
        private const val B56_FLAG = 0x0060

        private const val B789_FLAG = 0x0380
        private const val B456_FLAG = 0x0070
        private const val B012_FLAG = 0x0007
        private const val B89_FLAG = 0x0300

        private const val B7_FLAG = 0x0080
        private const val B4_FLAG = 0x0010
        private const val B0_FLAG = 0x0001

        /**
         * Converts an [Int] value between 0 and 999 into a DPD value.
         */
        fun of(value: Int): DenselyPackedDecimal {
            require(value in 0 .. 999) { "value outside DPD range" }

            val c = value % 10
            val b = (value / 10) % 10
            val a = value / 100
            val flag = ((a and 8) shr 1) or ((b and 8) shr 2) or ((c and 8) shr 3)
            val packed = when (flag) {
                0 -> (a shl 7) or (b shl 4) or c
                // one digit is 8-9
                1 -> (a shl 7) or (b shl 4) or c
                2 -> (a shl 7) or ((c and 6) shl 4) or ((b and 1) shl 4) or (c and 1) or 0x000A
                4 -> ((c and 6) shl 7) or ((a and 1) shl 7) or (b shl 4) or (c and 1) or 0x000C
                // two digits are 8-9
                3 -> (a shl 7) or ((b and 1) shl 4) or (c and 1) or 0x004E
                5 -> ((b and 6) shl 7) or ((a and 1) shl 7) or ((b and 1) shl 4) or (c and 1) or 0x002E
                6 -> ((c and 6) shl 7) or ((a and 1) shl 7) or ((b and 1) shl 4) or (c and 1) or 0x000E
                // all three digits 8-9
                else -> ((a and 1) shl 7) or ((b and 1) shl 4) or (c and 1) or 0x6E
            }.toShort()
            return DenselyPackedDecimal(packed)
        }

        /**
         * Converter instance that converts between integer values and their DPD representations.
         *
         * Forward conversions are done using [of], reverse conversions using [DenselyPackedDecimal.asNumber].
         */
        val converter = Converter.of(DenselyPackedDecimal::of, {it.asNumber})
    }

    private val triplet by lazy {
        bits.toInt().let {
            if (it and B3_FLAG == 0) {
                // 3 digits 0-7
                val a = (it and B789_FLAG) shr 7
                val b = (it and B456_FLAG) shr 4
                val c = (it and B012_FLAG)
                Triple(a, b, c)
            } else when ((it and B12_FLAG) shr 1) {
                0 -> {
                    // last digit 8-9
                    val a = (it and B789_FLAG) shr 7
                    val b = (it and B456_FLAG) shr 4
                    val c = 8 + (it and B0_FLAG)
                    Triple(a, b, c)
                }
                1 -> {
                    // middle digit 8-9
                    val a = (it and B789_FLAG) shr 7
                    val b = 8 + ((it and B4_FLAG) shr 4)
                    val c = ((it and B56_FLAG) shr 4) or (it and B0_FLAG)
                    Triple(a, b, c)
                }
                2 -> {
                    // first digit 8-9
                    val a = 8 + ((it and B7_FLAG) shr 7)
                    val b = (it and B456_FLAG) shr 4
                    val c = ((it and B89_FLAG) shr 7) or (it and B0_FLAG)
                    Triple(a, b, c)
                }
                else -> {
                    when ((it and B56_FLAG) shr 5) {
                        0 -> {
                            // first 2 digits 8-9
                            val a = 8 + ((it and B7_FLAG) shr 7)
                            val b = 8 + ((it and B4_FLAG) shr 4)
                            val c = ((it and B89_FLAG) shr 7) or (it and B0_FLAG)
                            Triple(a, b, c)
                        }
                        1 -> {
                            // first and last digits 8-9
                            val a = 8 + ((it and B7_FLAG) shr 7)
                            val b = ((it and B89_FLAG) shr 7) or ((it and B4_FLAG) shr 4)
                            val c = 8 + (it and B0_FLAG)
                            Triple(a, b, c)
                        }
                        2 -> {
                            // last 2 digits 8-9
                            val a = (it and B789_FLAG) shr 7
                            val b = 8 + ((it and B4_FLAG) shr 4)
                            val c = 8 + (it and B0_FLAG)
                            Triple(a, b, c)
                        }
                        else -> {
                            // all 3 digits 8-9
                            val a = 8 + ((it and B7_FLAG) shr 7)
                            val b = 8 + ((it and B4_FLAG) shr 4)
                            val c = 8 + (it and B0_FLAG)
                            Triple(a, b, c)
                        }
                    }
                }
            }
        }
    }

    /**
     * Returns this DPD value as an integer between 0 and 999.
     */
    val asNumber by lazy {
        triplet.let { (a, b, c) -> (100 * a) + (10 * b) + c }
    }

    /**
     * Returns this DPD value as an encoded string (ie. with leading zeros where applicable)
     */
    val asString by lazy {
        triplet.let { (a, b, c) -> "${a.toString().first()}${b.toString().first()}${c.toString().first()}" }
    }
}
