package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldBeEmpty
import io.kotest.matchers.collections.shouldContainInOrder
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainAll
import io.kotest.matchers.types.shouldBeSameInstanceAs
import io.kotest.property.Arb
import io.kotest.property.arbitrary.boolean
import io.kotest.property.arbitrary.list
import io.kotest.property.arbitrary.map
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder

class CollectionProviderExtensionsSpec : FunSpec() {
    init {
        test("getting key") {
            val project = ProjectBuilder.builder().build()
            val underlying = mockk<Map<String, String>>(relaxed = true)
            val provider = project.providers.provider { underlying }
            val slot = slot<String>()
            val key = "foo"

            val result = provider.getting(key)
            result.get()

            verify {
                underlying[capture(slot)]
            }
            slot.captured shouldBeEqual key
        }

        test("getting provider") {
            val project = ProjectBuilder.builder().build()
            val underlying = mockk<Map<String, String>>(relaxed = true)
            val provider = project.providers.provider { underlying }
            val slot = slot<String>()
            val key = "foo"
            val keyProvider = project.providers.provider { key }

            val result = provider.getting(keyProvider)
            result.get()

            verify {
                underlying[capture(slot)]
            }
            slot.captured shouldBeEqual key
        }

        test("mapElements") {
            val project = ProjectBuilder.builder().build()
            // Iterable::map is an inline extension function, which can't be mocked with mockk.
            // So we need an alternative to interation testing
            checkAll(Arb.list(Arb.string())) {
                val provider = project.providers.provider { it }
                val expected = it.map(String::length)

                val result = provider.mapElements(String::length)
                val actual = result.get()

                if (expected.isEmpty()) {
                    actual.shouldBeEmpty()
                } else {
                    actual shouldContainInOrder expected
                }
            }
        }

        test("mapElementsNotNull") {
            val project = ProjectBuilder.builder().build()
            // Iterable::mapNotNull is an inline extension function, which can't be mocked with mockk.
            // So we need an alternative to interation testing
            checkAll(Arb.list(Arb.string())) {
                val provider = project.providers.provider { it }
                val expected = it.map(String::length)

                val result = provider.mapElementsNotNull(String::length)
                val actual = result.get()

                if (expected.isEmpty()) {
                    actual.shouldBeEmpty()
                } else {
                    actual shouldContainInOrder expected
                }
            }
        }

        test("list orElseEmpty") {
            val project = ProjectBuilder.builder().build()
            val arb = Arb.boolean().map { listOf(it) }.orNull()
            checkAll(arb) {
                val provider: Provider<List<Boolean>> = project.providers.provider { it }

                val result = provider.orElseEmpty

                if (it == null) {
                    result.get().shouldBeEmpty()
                } else {
                    result.get() shouldBeSameInstanceAs it
                }
            }
        }

        test("set orElseEmpty") {
            val project = ProjectBuilder.builder().build()
            val arb = Arb.boolean().map { setOf(it) }.orNull()
            checkAll(arb) {
                val provider: Provider<Set<Boolean>> = project.providers.provider { it }

                val result = provider.orElseEmpty

                if (it == null) {
                    result.get().shouldBeEmpty()
                } else {
                    result.get() shouldBeSameInstanceAs it
                }
            }
        }

        test("map orElseEmpty") {
            val project = ProjectBuilder.builder().build()
            val arb = Arb.boolean().map { mapOf(it to it) }.orNull()
            checkAll(arb) {
                val provider: Provider<Map<Boolean, Boolean>> = project.providers.provider { it }

                val result = provider.orElseEmpty

                if (it == null) {
                    result.get().shouldBeEmpty()
                } else {
                    result.get() shouldBeSameInstanceAs it
                }
            }
        }

        test("properties asMap") {
            val project = ProjectBuilder.builder().build()
            val arb = Arb.map(Arb.string(), Arb.string())
            checkAll(arb) {
                val underlying = it.toProperties()
                val provider = project.providers.provider { underlying }

                val result = provider.asMap

                result.get() shouldContainAll it
            }
        }
    }
}
