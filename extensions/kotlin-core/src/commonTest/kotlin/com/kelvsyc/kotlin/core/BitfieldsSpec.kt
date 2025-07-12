package com.kelvsyc.kotlin.core

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.property.checkAll
import kotlin.experimental.and
import kotlin.experimental.or

class BitfieldsSpec : FunSpec() {
    data class ImmutableHolder<T>(val value: T)
    data class MutableHolder<T>(var value: T)

    init {
        test("immutable Byte flag") {
            checkAll<Byte> {
                val holder = ImmutableHolder(it)
                val delegated by flag(holder::value, 1)

                delegated shouldBeEqual ((it and 0x02).toInt() != 0)
            }
        }

        test("mutable Byte flag") {
            checkAll<Byte> {
                val holder = MutableHolder(it)
                var delegated by flag(holder::value, 1)

                delegated = true

                holder.value shouldBeEqual (it or 0x02)
            }
        }

        test("immutable Short flag") {
            checkAll<Short> {
                val holder = ImmutableHolder(it)
                val delegated by flag(holder::value, 1)

                delegated shouldBeEqual ((it and 0x02).toInt() != 0)
            }
        }

        test("mutable Short flag") {
            checkAll<Short> {
                val holder = MutableHolder(it)
                var delegated by flag(holder::value, 1)

                delegated = true

                holder.value shouldBeEqual (it or 0x02)
            }
        }

        test("immutable Int flag") {
            checkAll<Int> {
                val holder = ImmutableHolder(it)
                val delegated by flag(holder::value, 1)

                delegated shouldBeEqual ((it and 0x02) != 0)
            }
        }

        test("mutable Int flag") {
            checkAll<Int> {
                val holder = MutableHolder(it)
                var delegated by flag(holder::value, 1)

                delegated = true

                holder.value shouldBeEqual (it or 0x02)
            }
        }

        test("immutable Long flag") {
            checkAll<Long> {
                val holder = ImmutableHolder(it)
                val delegated by flag(holder::value, 1)

                delegated shouldBeEqual ((it and 0x02) != 0L)
            }
        }

        test("mutable Long flag") {
            checkAll<Long> {
                val holder = MutableHolder(it)
                var delegated by flag(holder::value, 1)

                delegated = true

                holder.value shouldBeEqual (it or 0x02)
            }
        }

        test("immutable Byte") {
            checkAll<Byte> {
                val holder = ImmutableHolder(it)
                val delegated by bitfield(holder::value, 1, 4)

                delegated shouldBeEqual ((it and 0x1E).toInt() ushr 1).toByte()
            }
        }

        test("mutable Byte") {
            checkAll<Byte> {
                val holder = MutableHolder(it)
                var delegated by bitfield(holder::value, 1, 4)

                delegated = 0x0F

                holder.value shouldBeEqual (it or 0x1E)
            }
        }

        test("immutable Short") {
            checkAll<Short> {
                val holder = ImmutableHolder(it)
                val delegated by bitfield(holder::value, 1, 4)

                delegated shouldBeEqual ((it and 0x1E).toInt() ushr 1).toShort()
            }
        }

        test("mutable Short") {
            checkAll<Short> {
                val holder = MutableHolder(it)
                var delegated by bitfield(holder::value, 1, 4)

                delegated = 0x0F

                holder.value shouldBeEqual (it or 0x1E)
            }
        }

        test("immutable Int") {
            checkAll<Int> {
                val holder = ImmutableHolder(it)
                val delegated by bitfield(holder::value, 1, 4)

                delegated shouldBeEqual ((it and 0x1E) ushr 1)
            }
        }

        test("mutable Int") {
            checkAll<Int> {
                val holder = MutableHolder(it)
                var delegated by bitfield(holder::value, 1, 4)

                delegated = 0x0F

                holder.value shouldBeEqual (it or 0x1E)
            }
        }

        test("immutable Long") {
            checkAll<Long> {
                val holder = ImmutableHolder(it)
                val delegated by bitfield(holder::value, 1, 4)

                delegated shouldBeEqual ((it and 0x1E) ushr 1)
            }
        }

        test("mutable Long") {
            checkAll<Long> {
                val holder = MutableHolder(it)
                var delegated by bitfield(holder::value, 1, 4)

                delegated = 0x0F

                holder.value shouldBeEqual (it or 0x1E)
            }
        }
    }
}
