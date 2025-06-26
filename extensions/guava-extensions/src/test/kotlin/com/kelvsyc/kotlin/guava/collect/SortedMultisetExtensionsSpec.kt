package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.ImmutableSortedMultiset
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence

class SortedMultisetExtensionsSpec : FunSpec() {
    interface DummyComparable : Comparable<DummyComparable>

    init {
        test("buildSortedMultiset") {
            val builder = mockk<ImmutableSortedMultiset.Builder<DummyComparable>>(relaxed = true)
            val action = mockk<ImmutableSortedMultiset.Builder<DummyComparable>.() -> Unit>(relaxed = true)
            mockkStatic(ImmutableSortedMultiset::class) {
                every { ImmutableSortedMultiset.naturalOrder<DummyComparable>() } returns builder

                buildSortedMultiset(action)

                verifySequence {
                    ImmutableSortedMultiset.naturalOrder<DummyComparable>()
                    action.invoke(builder)
                    builder.build()
                }
            }
        }

        test("buildSortedMultiset comparator") {
            val comparator = mockk<Comparator<Any>>()
            val action = mockk<ImmutableSortedMultiset.Builder<Any>.() -> Unit>(relaxed = true)
            mockkConstructor(ImmutableSortedMultiset.Builder::class) {
                every { anyConstructed<ImmutableSortedMultiset.Builder<Any>>().build() } returns mockk()

                buildSortedMultiset(comparator, action)

                verifySequence {
                    val builder = anyConstructed<ImmutableSortedMultiset.Builder<Any>>()
                    // FIXME how do we verify one builder was created?
                    action.invoke(any<ImmutableSortedMultiset.Builder<Any>>())
                    builder.build()
                }
            }
        }

        test("emptySortedMultiset") {
            mockkStatic(ImmutableSortedMultiset::class) {
                every { ImmutableSortedMultiset.of<Any>() } returns mockk()

                emptySortedMultiset<Any>()

                verify {
                    ImmutableSortedMultiset.of<Any>()
                }
            }
        }

        test("sortedMultisetOf empty") {
            mockkStatic(ImmutableSortedMultiset::class) {
                every { ImmutableSortedMultiset.of<Any>() } returns mockk()

                sortedMultisetOf<Any>()

                verify {
                    ImmutableSortedMultiset.of<Any>()
                }
            }
        }

        test("sortedMultisetOf varargs") {
            val value = mockk<DummyComparable>()
            val slot = slot<Iterable<DummyComparable>>()
            mockkStatic(ImmutableSortedMultiset::class) {
                every { ImmutableSortedMultiset.copyOf(any<Comparator<DummyComparable>>(), any<Iterable<DummyComparable>>()) } returns mockk()

                sortedMultisetOf(value)

                verify {
                    ImmutableSortedMultiset.copyOf(naturalOrder(), capture(slot))
                }
                slot.captured.toList() shouldBe listOf(value)
            }
        }

        test("sortedMultisetOf comparator varargs") {
            val value = mockk<Any>()
            val comparator = mockk<Comparator<Any>>()
            val slot = slot<Iterable<Any>>()
            mockkStatic(ImmutableSortedMultiset::class) {
                every { ImmutableSortedMultiset.copyOf(any<Comparator<Any>>(), any<Iterable<Any>>()) } returns mockk()

                sortedMultisetOf(comparator, value)

                verify {
                    ImmutableSortedMultiset.copyOf(comparator, capture(slot))
                }
                slot.captured.toList() shouldBe listOf(value)
            }
        }
    }
}
