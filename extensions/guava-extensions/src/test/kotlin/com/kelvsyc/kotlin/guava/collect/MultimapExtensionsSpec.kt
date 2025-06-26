package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.ImmutableListMultimap
import com.google.common.collect.ImmutableMultimap
import com.google.common.collect.ImmutableSetMultimap
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifySequence

class MultimapExtensionsSpec : FunSpec() {
    init {
        test("buildMultimap") {
            val builder = mockk<ImmutableMultimap.Builder<Any, Any>>(relaxed = true)
            val action = mockk<ImmutableMultimap.Builder<Any, Any>.() -> Unit>(relaxed = true)
            mockkStatic(ImmutableMultimap::class) {
                every { ImmutableMultimap.builder<Any, Any>() } returns builder

                buildMultimap(action)

                verifySequence {
                    ImmutableMultimap.builder<Any, Any>()
                    action.invoke(builder)
                    builder.build()
                }
            }
        }

        test("emptyMultimap") {
            mockkStatic(ImmutableMultimap::class) {
                every { ImmutableMultimap.of<Any, Any>() } returns mockk()

                emptyMultimap<Any, Any>()

                verify {
                    ImmutableMultimap.of<Any, Any>()
                }
            }
        }

        test("multiMapOf 0") {
            mockkStatic(ImmutableMultimap::class) {
                every { ImmutableMultimap.of<Any, Any>() } returns mockk()

                multimapOf<Any, Any>()

                verify {
                    ImmutableMultimap.of<Any, Any>()
                }
            }
        }

        test("multiMapOf 1") {
            val k1 = mockk<Any>()
            val v1 = mockk<Any>()
            mockkStatic(ImmutableMultimap::class) {
                every { ImmutableMultimap.of<Any, Any>(any(), any()) } returns mockk()

                multimapOf(k1 to v1)

                verify {
                    ImmutableMultimap.of(k1, v1)
                }
            }
        }

        test("multiMapOf varargs") {
            val builder = mockk<ImmutableMultimap.Builder<Any, Any>>(relaxed = true)
            val k1 = mockk<Any>()
            val v1 = mockk<Any>()
            val k2 = mockk<Any>()
            val v2 = mockk<Any>()
            mockkStatic(ImmutableMultimap::class) {
                every { ImmutableMultimap.builder<Any, Any>() } returns builder

                multimapOf(k1 to v1, k2 to v2)

                verifySequence {
                    ImmutableMultimap.builder<Any, Any>()
                    builder.put(k1, v1)
                    builder.put(k2, v2)
                    builder.build()
                }
            }
        }

        test("buildListMultimap") {
            val builder = mockk<ImmutableListMultimap.Builder<Any, Any>>(relaxed = true)
            val action = mockk<ImmutableListMultimap.Builder<Any, Any>.() -> Unit>(relaxed = true)
            mockkStatic(ImmutableListMultimap::class) {
                every { ImmutableListMultimap.builder<Any, Any>() } returns builder

                buildListMultimap(action)

                verifySequence {
                    ImmutableListMultimap.builder<Any, Any>()
                    action.invoke(builder)
                    builder.build()
                }
            }
        }

        test("emptyListMultimap") {
            mockkStatic(ImmutableListMultimap::class) {
                every { ImmutableListMultimap.of<Any, Any>() } returns mockk()

                emptyListMultimap<Any, Any>()

                verify {
                    ImmutableListMultimap.of<Any, Any>()
                }
            }
        }

        test("listMultimapOf 0") {
            mockkStatic(ImmutableListMultimap::class) {
                every { ImmutableListMultimap.of<Any, Any>() } returns mockk()

                listMultimapOf<Any, Any>()

                verify {
                    ImmutableListMultimap.of<Any, Any>()
                }
            }
        }

        test("listMultimapOf varargs") {
            val builder = mockk<ImmutableListMultimap.Builder<Any, Any>>(relaxed = true)
            val k1 = mockk<Any>()
            val v1 = mockk<Any>()
            mockkStatic(ImmutableListMultimap::class) {
                every { ImmutableListMultimap.builder<Any, Any>() } returns builder

                listMultimapOf(k1 to v1)

                verifySequence {
                    ImmutableListMultimap.builder<Any, Any>()
                    builder.put(k1, v1)
                    builder.build()
                }
            }
        }

        test("buildSetMultimap") {
            val builder = mockk<ImmutableSetMultimap.Builder<Any, Any>>(relaxed = true)
            val action = mockk<ImmutableSetMultimap.Builder<Any, Any>.() -> Unit>(relaxed = true)
            mockkStatic(ImmutableSetMultimap::class) {
                every { ImmutableSetMultimap.builder<Any, Any>() } returns builder

                buildSetMultimap(action)

                verifySequence {
                    ImmutableSetMultimap.builder<Any, Any>()
                    action.invoke(builder)
                    builder.build()
                }
            }
        }

        test("emptySetMultimap") {
            mockkStatic(ImmutableSetMultimap::class) {
                every { ImmutableSetMultimap.of<Any, Any>() } returns mockk()

                emptySetMultimap<Any, Any>()

                verify {
                    ImmutableSetMultimap.of<Any, Any>()
                }
            }
        }

        test("setMultimapOf 0") {
            mockkStatic(ImmutableSetMultimap::class) {
                every { ImmutableSetMultimap.of<Any, Any>() } returns mockk()

                setMultimapOf<Any, Any>()

                verify {
                    ImmutableSetMultimap.of<Any, Any>()
                }
            }
        }

        test("setMultimapOf varargs") {
            val builder = mockk<ImmutableSetMultimap.Builder<Any, Any>>(relaxed = true)
            val k1 = mockk<Any>()
            val v1 = mockk<Any>()
            mockkStatic(ImmutableSetMultimap::class) {
                every { ImmutableSetMultimap.builder<Any, Any>() } returns builder

                setMultimapOf(k1 to v1)

                verifySequence {
                    ImmutableSetMultimap.builder<Any, Any>()
                    builder.put(k1, v1)
                    builder.build()
                }
            }
        }
    }
}
