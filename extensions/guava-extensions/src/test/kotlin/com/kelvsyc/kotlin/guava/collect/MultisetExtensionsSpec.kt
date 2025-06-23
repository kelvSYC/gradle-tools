package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.EnumMultiset
import com.google.common.collect.HashMultiset
import com.google.common.collect.ImmutableMultiset
import com.google.common.collect.LinkedHashMultiset
import com.google.common.collect.TreeMultiset
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.slot
import io.mockk.verify
import io.mockk.verifySequence

class MultisetExtensionsSpec : FunSpec() {
    enum class Dummy { FOO }
    interface ComparableDummy : Comparable<ComparableDummy>

    init {
        test("buildMultiset") {
            val builder = mockk<ImmutableMultiset.Builder<Any>>(relaxed = true)
            val action = mockk<ImmutableMultiset.Builder<Any>.() -> Unit>(relaxed = true)
            mockkStatic(ImmutableMultiset::class) {
                every { ImmutableMultiset.builder<Any>() } returns builder

                buildMultiset(action)

                verifySequence {
                    ImmutableMultiset.builder<Any>()
                    action.invoke(builder)
                    builder.build()
                }
            }
        }

        test("emptyMultiset") {
            mockkStatic(ImmutableMultiset::class) {
                every { ImmutableMultiset.of<Any>() } returns mockk()

                emptyMultiset<Any>()

                verify {
                    ImmutableMultiset.of<Any>()
                }
            }
        }

        test("multisetOf 0") {
            mockkStatic(ImmutableMultiset::class) {
                every { ImmutableMultiset.of<Any>() } returns mockk()

                multisetOf<Any>()

                verify {
                    ImmutableMultiset.of<Any>()
                }
            }
        }

        test("multisetOf 1") {
            val e1 = mockk<Any>()
            mockkStatic(ImmutableMultiset::class) {
                every { ImmutableMultiset.of<Any>(any()) } returns mockk()

                multisetOf(e1)

                verify {
                    ImmutableMultiset.of(e1)
                }
            }
        }

        test("multisetOf 2") {
            val e1 = mockk<Any>()
            val e2 = mockk<Any>()
            mockkStatic(ImmutableMultiset::class) {
                every { ImmutableMultiset.of<Any>(any(), any()) } returns mockk()

                multisetOf(e1, e2)

                verify {
                    ImmutableMultiset.of(e1, e2)
                }
            }
        }

        test("multisetOf 3") {
            val e1 = mockk<Any>()
            val e2 = mockk<Any>()
            val e3 = mockk<Any>()
            mockkStatic(ImmutableMultiset::class) {
                every { ImmutableMultiset.of<Any>(any(), any(), any()) } returns mockk()

                multisetOf(e1, e2, e3)

                verify {
                    ImmutableMultiset.of(e1, e2, e3)
                }
            }
        }

        test("multisetOf 4") {
            val e1 = mockk<Any>()
            val e2 = mockk<Any>()
            val e3 = mockk<Any>()
            val e4 = mockk<Any>()
            mockkStatic(ImmutableMultiset::class) {
                every { ImmutableMultiset.of<Any>(any(), any(), any(), any()) } returns mockk()

                multisetOf(e1, e2, e3, e4)

                verify {
                    ImmutableMultiset.of(e1, e2, e3, e4)
                }
            }
        }

        test("multisetOf 5") {
            val e1 = mockk<Any>()
            val e2 = mockk<Any>()
            val e3 = mockk<Any>()
            val e4 = mockk<Any>()
            val e5 = mockk<Any>()
            mockkStatic(ImmutableMultiset::class) {
                every { ImmutableMultiset.of<Any>(any(), any(), any(), any(), any()) } returns mockk()

                multisetOf(e1, e2, e3, e4, e5)

                verify {
                    ImmutableMultiset.of(e1, e2, e3, e4, e5)
                }
            }
        }

        test("multisetOf varargs") {
            val builder = mockk<ImmutableMultiset.Builder<Any>>(relaxed = true)
            val e1 = mockk<Any>()
            val e2 = mockk<Any>()
            val e3 = mockk<Any>()
            val e4 = mockk<Any>()
            val e5 = mockk<Any>()
            val e6 = mockk<Any>()
            val slot = slot<Iterable<Any>>()
            mockkStatic(ImmutableMultiset::class) {
                every { ImmutableMultiset.builder<Any>() } returns builder

                multisetOf(e1, e2, e3, e4, e5, e6)

                verifySequence {
                    ImmutableMultiset.builder<Any>()
                    builder.addAll(capture(slot))
                    builder.build()
                }
                slot.captured.toList() shouldBe listOf(e1, e2, e3, e4, e5, e6)
            }
        }

        test("enumMultisetOf empty") {
            mockkStatic(EnumMultiset::class) {
                every { EnumMultiset.create(Dummy::class.java) } returns mockk()

                enumMultisetOf<Dummy>()

                verify {
                    EnumMultiset.create(Dummy::class.java)
                }
            }
        }

        test("enumMultisetOf varargs") {
            val slot = slot<Iterable<Dummy>>()
            mockkStatic(EnumMultiset::class) {
                every { EnumMultiset.create(any<Iterable<Dummy>>(), Dummy::class.java) } returns mockk()

                enumMultisetOf(Dummy.FOO)

                verify {
                    EnumMultiset.create(capture(slot), Dummy::class.java)
                }
                slot.captured.toList() shouldBe listOf(Dummy.FOO)
            }
        }

        test("hashMultisetOf empty") {
            mockkStatic(HashMultiset::class) {
                every { HashMultiset.create<Any>() } returns mockk()

                hashMultisetOf<Any>()

                verify {
                    HashMultiset.create<Any>()
                }
            }
        }

        test("hashMultisetOf varargs") {
            val slot = slot<Iterable<Any>>()
            val value = mockk<Any>()
            mockkStatic(HashMultiset::class) {
                every { HashMultiset.create(any<Iterable<Any>>()) } returns mockk()

                hashMultisetOf(value)

                verify {
                    HashMultiset.create(capture(slot))
                }
                slot.captured.toList() shouldBe listOf(value)
            }
        }

        test("linkedMultisetOf empty") {
            mockkStatic(LinkedHashMultiset::class) {
                every { LinkedHashMultiset.create<Any>() } returns mockk()

                linkedMultisetOf<Any>()

                verify {
                    LinkedHashMultiset.create<Any>()
                }
            }
        }

        test("linkedMultisetOf varargs") {
            val slot = slot<Iterable<Any>>()
            val value = mockk<Any>()
            mockkStatic(LinkedHashMultiset::class) {
                every { LinkedHashMultiset.create(any<Iterable<Any>>()) } returns mockk()

                linkedMultisetOf(value)

                verify {
                    LinkedHashMultiset.create(capture(slot))
                }
                slot.captured.toList() shouldBe listOf(value)
            }
        }

        test("treeMultisetOf empty") {
            mockkStatic(TreeMultiset::class) {
                every { TreeMultiset.create<ComparableDummy>() } returns mockk()

                treeMultisetOf<ComparableDummy>()

                verify {
                    TreeMultiset.create<ComparableDummy>()
                }
            }
        }

        test("treeMultisetOf varargs") {
            val slot = slot<Iterable<ComparableDummy>>()
            val value = mockk<ComparableDummy>()
            mockkStatic(TreeMultiset::class) {
                every { TreeMultiset.create(any<Iterable<ComparableDummy>>()) } returns mockk()

                treeMultisetOf(value)

                verify {
                    TreeMultiset.create(capture(slot))
                }
                slot.captured.toList() shouldBe listOf(value)
            }
        }

        test("treeMultisetOf comparator") {
            val comparator = mockk<Comparator<Any>>()
            mockkStatic(TreeMultiset::class) {
                every { TreeMultiset.create(any<Comparator<Any>>()) } returns mockk()

                treeMultisetOf(comparator)

                verify {
                    TreeMultiset.create(comparator)
                }
            }
        }

        test("treeMultisetOf comparator varargs") {
            val comparator = mockk<Comparator<Any>>()
            val value = mockk<Any>()
            val result = mockk<TreeMultiset<Any>>(relaxed = true)
            val slot = slot<List<Any>>()
            mockkStatic(TreeMultiset::class) {
                every { TreeMultiset.create<Any>(any<Comparator<Any>>()) } returns result

                treeMultisetOf(comparator, value)

                verify {
                    TreeMultiset.create(comparator)
                    result.addAll(capture(slot))
                }
                slot.captured shouldBe listOf(value)
            }
        }
    }
}
