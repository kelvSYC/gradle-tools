package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.ImmutableRangeMap
import com.google.common.collect.Range
import com.google.common.collect.TreeRangeMap
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifySequence

class RangeMapExtensionsSpec : FunSpec() {
    interface DummyComparable : Comparable<DummyComparable>

    init {
        test("buildRangeMap") {
            val builder = mockk<ImmutableRangeMap.Builder<DummyComparable, Any>>(relaxed = true)
            val action = mockk<ImmutableRangeMap.Builder<DummyComparable, Any>.() -> Unit>(relaxed = true)
            mockkStatic(ImmutableRangeMap::class) {
                every { ImmutableRangeMap.builder<DummyComparable, Any>() } returns builder

                buildRangeMap<DummyComparable, Any>(action)

                verifySequence {
                    ImmutableRangeMap.builder<DummyComparable, Any>()
                    action.invoke(builder)
                    builder.build()
                }
            }
        }

        test("emptyRangeMap") {
            mockkStatic(ImmutableRangeMap::class) {
                every { ImmutableRangeMap.of<DummyComparable, Any>() } returns mockk()

                emptyRangeMap<DummyComparable, Any>()

                verify {
                    ImmutableRangeMap.of<DummyComparable, Any>()
                }
            }
        }

        test("rangeMapOf 0") {
            mockkStatic(ImmutableRangeMap::class) {
                every { ImmutableRangeMap.of<DummyComparable, Any>() } returns mockk()

                rangeMapOf<DummyComparable, Any>()

                verify {
                    ImmutableRangeMap.of<DummyComparable, Any>()
                }
            }
        }

        test("rangeMapOf 1") {
            val k1 = mockk<Range<DummyComparable>>()
            val v1 = mockk<Any>()
            mockkStatic(ImmutableRangeMap::class) {
                every { ImmutableRangeMap.of<DummyComparable, Any>(any(), any()) } returns mockk()

                rangeMapOf(k1 to v1)

                verify {
                    ImmutableRangeMap.of(k1, v1)
                }
            }
        }

        test("rangeMapOf varargs") {
            val builder = mockk<ImmutableRangeMap.Builder<DummyComparable, Any>>(relaxed = true)
            val k1 = mockk<Range<DummyComparable>>()
            val v1 = mockk<Any>()
            val k2 = mockk<Range<DummyComparable>>()
            val v2 = mockk<Any>()
            mockkStatic(ImmutableRangeMap::class) {
                every { ImmutableRangeMap.builder<DummyComparable, Any>() } returns builder

                rangeMapOf(k1 to v1, k2 to v2)

                verifySequence {
                    ImmutableRangeMap.builder<DummyComparable, Any>()
                    builder.put(k1, v1)
                    builder.put(k2, v2)
                    builder.build()
                }
            }
        }

        test("treeRangeMapOf 0") {
            mockkStatic(TreeRangeMap::class) {
                every { TreeRangeMap.create<DummyComparable, Any>() } returns mockk()

                treeRangeMapOf<DummyComparable, Any>()

                verify {
                    TreeRangeMap.create<DummyComparable, Any>()
                }
            }
        }

        test("treeRangeMapOf varargs") {
            val result = mockk<TreeRangeMap<DummyComparable, Any>>(relaxed = true)
            val k1 = mockk<Range<DummyComparable>>()
            val v1 = mockk<Any>()
            mockkStatic(TreeRangeMap::class) {
                every { TreeRangeMap.create<DummyComparable, Any>() } returns result

                treeRangeMapOf(k1 to v1)

                verifySequence {
                    TreeRangeMap.create<DummyComparable, Any>()
                    result.put(k1, v1)
                }
            }
        }
    }
}
