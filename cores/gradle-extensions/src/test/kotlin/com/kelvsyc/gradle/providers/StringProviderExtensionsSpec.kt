package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.string.shouldBeEmpty
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.orNull
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.of
import org.gradle.api.provider.Provider
import org.gradle.testfixtures.ProjectBuilder

class StringProviderExtensionsSpec : FunSpec() {
    init {
        test("filterNotBlank") {
            val project = ProjectBuilder.builder().build()
            checkAll(Exhaustive.of("", " ", "1")) {
                val base = project.providers.provider { it }

                val result = base.filterNotBlank()

                if (it.isBlank()) {
                    result.isPresent.shouldBeFalse()
                } else {
                    result.isPresent.shouldBeTrue()
                }
            }
        }

        test("orElseEmpty") {
            val project = ProjectBuilder.builder().build()
            checkAll(Arb.string().orNull()) {
                val base: Provider<String> = project.providers.provider { it }

                val result = base.orElseEmpty

                result.isPresent.shouldBeTrue()
                if (it == null) {
                    result.get().shouldBeEmpty()
                } else {
                    result.get() shouldBeEqual it
                }
            }
        }

        test("asInt") {
            val project = ProjectBuilder.builder().build()
            checkAll<Int>() {
                val base = it.toString()
                val provider = project.providers.provider { base }

                val result = provider.asInt

                result.get() shouldBeEqual it
            }
        }

        test("asBoolean") {
            val project = ProjectBuilder.builder().build()
            checkAll<Boolean>() {
                val base = it.toString()
                val provider = project.providers.provider { base }

                val result = provider.asBoolean

                result.get() shouldBeEqual it
            }
        }
    }
}
