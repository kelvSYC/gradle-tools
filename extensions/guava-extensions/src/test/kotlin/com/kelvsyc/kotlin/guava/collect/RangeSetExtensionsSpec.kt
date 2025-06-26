package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.ImmutableRangeSet
import com.google.common.collect.Range
import com.google.common.collect.TreeRangeSet
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence

class RangeSetExtensionsSpec : FunSpec() {
    interface DummyComparable : Comparable<DummyComparable>

    init {
        test("buildRangeSet") {
            val builder = mockk<ImmutableRangeSet.Builder<DummyComparable>>(relaxed = true)
            val action = mockk<ImmutableRangeSet.Builder<DummyComparable>.() -> Unit>(relaxed = true)
            mockkStatic(ImmutableRangeSet::class) {
                every { ImmutableRangeSet.builder<DummyComparable>() } returns builder

                buildRangeSet(action)

                verifySequence {
                    ImmutableRangeSet.builder<DummyComparable>()
                    action.invoke(builder)
                    builder.build()
                }
            }
        }

        test("emptyRangeSet") {
            mockkStatic(ImmutableRangeSet::class) {
                every { ImmutableRangeSet.of<DummyComparable>() } returns mockk()

                emptyRangeSet<DummyComparable>()

                verify {
                    ImmutableRangeSet.of<DummyComparable>()
                }
            }
        }

        test("rangeSetOf 0") {
            mockkStatic(ImmutableRangeSet::class) {
                every { ImmutableRangeSet.of<DummyComparable>() } returns mockk()

                rangeSetOf<DummyComparable>()

                verify {
                    ImmutableRangeSet.of<DummyComparable>()
                }
            }
        }

        test("rangeSetOf 1") {
            val value = mockk<Range<DummyComparable>>()
            mockkStatic(ImmutableRangeSet::class) {
                every { ImmutableRangeSet.of(any<Range<DummyComparable>>()) } returns mockk()

                rangeSetOf(value)

                verify {
                    ImmutableRangeSet.of(value)
                }
            }
        }

        test("rangeSetOf varargs") {
            val value1 = mockk<Range<DummyComparable>>()
            val value2 = mockk<Range<DummyComparable>>()
            val slot = slot<Iterable<Range<DummyComparable>>>()
            mockkStatic(ImmutableRangeSet::class) {
                every { ImmutableRangeSet.unionOf(any<Iterable<Range<DummyComparable>>>()) } returns mockk()

                rangeSetOf(value1, value2)

                verify {
                    ImmutableRangeSet.unionOf<DummyComparable>(capture(slot))
                }
                slot.captured.toList() shouldBe listOf(value1, value2)
            }
        }

        test("treeRangeSetOf 0") {
            mockkStatic(TreeRangeSet::class) {
                every { TreeRangeSet.create<DummyComparable>() } returns mockk()

                treeRangeSetOf<DummyComparable>()

                verify {
                    TreeRangeSet.create<DummyComparable>()
                }
            }
        }

        test("treeRangeSetOf varargs") {
            val value = mockk<Range<DummyComparable>>()
            val slot = slot<Iterable<Range<DummyComparable>>>()
            mockkStatic(TreeRangeSet::class) {
                every { TreeRangeSet.create(any<Iterable<Range<DummyComparable>>>()) } returns mockk()

                treeRangeSetOf(value)

                verifySequence {
                    TreeRangeSet.create(capture(slot))
                }
                slot.captured.toList() shouldBe listOf(value)
            }
        }
    }
}
