package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.EnumBiMap
import com.google.common.collect.EnumHashBiMap
import com.google.common.collect.HashBiMap
import com.google.common.collect.ImmutableBiMap
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifySequence

class BimapExtensionsSpec : FunSpec() {
    enum class Dummy { FOO, BAR }

    init {
        test("buildBiMap") {
            val builder = mockk<ImmutableBiMap.Builder<Any, Any>>(relaxed = true)
            val action = mockk<ImmutableBiMap.Builder<Any, Any>.() -> Unit>(relaxed = true)
            mockkStatic(ImmutableBiMap::class) {
                every { ImmutableBiMap.builder<Any, Any>() } returns builder

                buildBiMap(action)

                verifySequence {
                    ImmutableBiMap.builder<Any, Any>()
                    action.invoke(builder)
                    builder.build()
                }
            }
        }

        test("emptyBiMap") {
            mockkStatic(ImmutableBiMap::class) {
                every { ImmutableBiMap.of<Any, Any>() } returns mockk()

                emptyBiMap<Any, Any>()

                verify {
                    ImmutableBiMap.of<Any, Any>()
                }
            }
        }

        test("biMapOf 0") {
            mockkStatic(ImmutableBiMap::class) {
                every { ImmutableBiMap.of<Any, Any>() } returns mockk()

                biMapOf<Any, Any>()

                verify {
                    ImmutableBiMap.of<Any, Any>()
                }
            }
        }

        test("biMapOf 1") {
            val k1 = mockk<Any>()
            val v1 = mockk<Any>()
            mockkStatic(ImmutableBiMap::class) {
                every { ImmutableBiMap.of<Any, Any>(any(), any()) } returns mockk()

                biMapOf<Any, Any>(k1 to v1)

                verify {
                    ImmutableBiMap.of<Any, Any>(k1, v1)
                }
            }
        }

        test("biMapOf 2") {
            val k1 = mockk<Any>()
            val v1 = mockk<Any>()
            val k2 = mockk<Any>()
            val v2 = mockk<Any>()
            mockkStatic(ImmutableBiMap::class) {
                every { ImmutableBiMap.of<Any, Any>(any(), any(), any(), any()) } returns mockk()

                biMapOf<Any, Any>(k1 to v1, k2 to v2)

                verify {
                    ImmutableBiMap.of<Any, Any>(k1, v1, k2, v2)
                }
            }
        }

        test("biMapOf 3") {
            val k1 = mockk<Any>()
            val v1 = mockk<Any>()
            val k2 = mockk<Any>()
            val v2 = mockk<Any>()
            val k3 = mockk<Any>()
            val v3 = mockk<Any>()
            mockkStatic(ImmutableBiMap::class) {
                every { ImmutableBiMap.of<Any, Any>(any(), any(), any(), any(), any(), any()) } returns mockk()

                biMapOf<Any, Any>(k1 to v1, k2 to v2, k3 to v3)

                verify {
                    ImmutableBiMap.of<Any, Any>(k1, v1, k2, v2, k3, v3)
                }
            }
        }

        test("biMapOf 4") {
            val k1 = mockk<Any>()
            val v1 = mockk<Any>()
            val k2 = mockk<Any>()
            val v2 = mockk<Any>()
            val k3 = mockk<Any>()
            val v3 = mockk<Any>()
            val k4 = mockk<Any>()
            val v4 = mockk<Any>()
            mockkStatic(ImmutableBiMap::class) {
                every { ImmutableBiMap.of<Any, Any>(any(), any(), any(), any(), any(), any(), any(), any()) } returns mockk()

                biMapOf<Any, Any>(k1 to v1, k2 to v2, k3 to v3, k4 to v4)

                verify {
                    ImmutableBiMap.of<Any, Any>(k1, v1, k2, v2, k3, v3, k4, v4)
                }
            }
        }

        test("biMapOf 5") {
            val k1 = mockk<Any>()
            val v1 = mockk<Any>()
            val k2 = mockk<Any>()
            val v2 = mockk<Any>()
            val k3 = mockk<Any>()
            val v3 = mockk<Any>()
            val k4 = mockk<Any>()
            val v4 = mockk<Any>()
            val k5 = mockk<Any>()
            val v5 = mockk<Any>()
            mockkStatic(ImmutableBiMap::class) {
                every { ImmutableBiMap.of<Any, Any>(any(), any(), any(), any(), any(), any(), any(), any(), any(), any()) } returns mockk()

                biMapOf<Any, Any>(k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5)

                verify {
                    ImmutableBiMap.of<Any, Any>(k1, v1, k2, v2, k3, v3, k4, v4, k5, v5)
                }
            }
        }

        test("biMapOf varargs") {
            val builder = mockk<ImmutableBiMap.Builder<Any, Any>>(relaxed = true)
            val k1 = mockk<Any>()
            val v1 = mockk<Any>()
            val k2 = mockk<Any>()
            val v2 = mockk<Any>()
            val k3 = mockk<Any>()
            val v3 = mockk<Any>()
            val k4 = mockk<Any>()
            val v4 = mockk<Any>()
            val k5 = mockk<Any>()
            val v5 = mockk<Any>()
            val k6 = mockk<Any>()
            val v6 = mockk<Any>()
            mockkStatic(ImmutableBiMap::class) {
                every { ImmutableBiMap.builder<Any, Any>() } returns builder

                biMapOf<Any, Any>(k1 to v1, k2 to v2, k3 to v3, k4 to v4, k5 to v5, k6 to v6)

                verifySequence {
                    ImmutableBiMap.builder<Any, Any>()
                    builder.put(k1, v1)
                    builder.put(k2, v2)
                    builder.put(k3, v3)
                    builder.put(k4, v4)
                    builder.put(k5, v5)
                    builder.put(k6, v6)
                    builder.build()
                }
            }
        }

        test("enumBiMapOf empty") {
            mockkStatic(EnumBiMap::class) {
                every { EnumBiMap.create(Dummy::class.java, Dummy::class.java) } returns mockk()

                enumBiMapOf<Dummy, Dummy>()

                verify {
                    EnumBiMap.create(Dummy::class.java, Dummy::class.java)
                }
            }
        }

        test("enumBiMapOf varargs") {
            mockkStatic(EnumBiMap::class) {
                every { EnumBiMap.create(any<Map<Dummy, Dummy>>()) } returns mockk()

                enumBiMapOf(Dummy.FOO to Dummy.BAR)

                verify {
                    EnumBiMap.create(mapOf(Dummy.FOO to Dummy.BAR))
                }
            }
        }

        test("enumHashBiMapOf empty") {
            mockkStatic(EnumHashBiMap::class) {
                every { EnumHashBiMap.create<Dummy, Any>(Dummy::class.java) } returns mockk()

                enumHashBiMapOf<Dummy, Any>()

                verify {
                    EnumHashBiMap.create<Dummy, Any>(Dummy::class.java)
                }
            }
        }

        test("enumHashBiMapOf varargs") {
            val value = mockk<Any>()
            mockkStatic(EnumHashBiMap::class) {
                every { EnumHashBiMap.create(any<Map<Dummy, Any>>()) } returns mockk()

                enumHashBiMapOf(Dummy.FOO to value)

                verify {
                    EnumHashBiMap.create(mapOf(Dummy.FOO to value))
                }
            }
        }

        test("hashBiMapOf empty") {
            mockkStatic(HashBiMap::class) {
                every { HashBiMap.create<Any, Any>() } returns mockk()

                hashBiMapOf<Any, Any>()

                verify {
                    HashBiMap.create<Any, Any>()
                }
            }
        }

        test("hashBiMapOf varargs") {
            val key = mockk<Any>()
            val value = mockk<Any>()
            mockkStatic(HashBiMap::class) {
                every { HashBiMap.create(any<Map<Any, Any>>()) } returns mockk()

                hashBiMapOf(key to value)

                verify {
                    HashBiMap.create(mapOf(key to value))
                }
            }
        }
    }
}
