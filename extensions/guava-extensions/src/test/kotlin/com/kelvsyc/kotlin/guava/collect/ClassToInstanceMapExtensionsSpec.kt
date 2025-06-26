package com.kelvsyc.kotlin.guava.collect

import com.google.common.collect.ClassToInstanceMap
import com.google.common.collect.ImmutableClassToInstanceMap
import com.google.common.collect.MutableClassToInstanceMap
import com.google.common.reflect.ImmutableTypeToInstanceMap
import com.google.common.reflect.MutableTypeToInstanceMap
import com.google.common.reflect.TypeToInstanceMap
import com.kelvsyc.kotlin.guava.reflect.typeTokenOf
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.mockkStatic
import io.mockk.verify
import io.mockk.verifySequence

class ClassToInstanceMapExtensionsSpec : FunSpec() {
    init {
        test("class get") {
            val map = mockk<ClassToInstanceMap<Any>>(relaxed = true)

            map.get<_, Any>()

            verify {
                map.getInstance(Any::class.java)
            }
        }

        test("class put immutable builder") {
            val builder = mockk<ImmutableClassToInstanceMap.Builder<Any>>(relaxed = true)
            val value = mockk<Any>()

            builder.put(value)

            verify {
                builder.put(Any::class.java, value)
            }
        }

        test("class put mutable") {
            val map = mockk<MutableClassToInstanceMap<Any>>(relaxed = true)
            val value = mockk<Any>()

            map.put(value)

            verify {
                map.putInstance(Any::class.java, value)
            }
        }

        test("class builder") {
            val builder = mockk<ImmutableClassToInstanceMap.Builder<Any>>(relaxed = true)
            val action = mockk<ImmutableClassToInstanceMap.Builder<Any>.() -> Unit>(relaxed = true)

            mockkStatic(ImmutableClassToInstanceMap::class) {
                every { ImmutableClassToInstanceMap.builder<Any>() } returns builder

                buildClassToInstanceMap(action)

                verifySequence {
                    ImmutableClassToInstanceMap.builder<Any>()
                    action.invoke(builder)
                    builder.build()
                }
            }
        }

        test("type get") {
            val map = mockk<TypeToInstanceMap<Any>>(relaxed = true)

            map.get<_, Any>()

            verify {
                map.getInstance(typeTokenOf<Any>())
            }
        }

        test("type put immutable builder") {
            val builder = mockk<ImmutableTypeToInstanceMap.Builder<Any>>(relaxed = true)
            val value = mockk<Any>()

            builder.put(value)

            verify {
                builder.put(typeTokenOf<Any>(), value)
            }
        }

        test("type put mutable") {
            val map = mockk<MutableTypeToInstanceMap<Any>>(relaxed = true)
            val value = mockk<Any>()

            map.put(value)

            verify {
                map.putInstance(typeTokenOf<Any>(), value)
            }
        }

        test("type builder") {
            val builder = mockk<ImmutableTypeToInstanceMap.Builder<Any>>(relaxed = true)
            val action = mockk<ImmutableTypeToInstanceMap.Builder<Any>.() -> Unit>(relaxed = true)

            mockkStatic(ImmutableTypeToInstanceMap::class) {
                every { ImmutableTypeToInstanceMap.builder<Any>() } returns builder

                buildTypeToInstanceMap(action)

                verifySequence {
                    ImmutableTypeToInstanceMap.builder<Any>()
                    action.invoke(builder)
                    builder.build()
                }
            }
        }
    }
}
