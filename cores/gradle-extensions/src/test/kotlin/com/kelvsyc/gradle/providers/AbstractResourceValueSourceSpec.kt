package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import org.gradle.testfixtures.ProjectBuilder

class AbstractResourceValueSourceSpec : FunSpec() {
    init {
        test("StringResourceValueSource returns resource content") {
            val project = ProjectBuilder.builder().build()

            val provider = project.providers.of(StringResourceValueSource::class.java) {
                it.parameters.resourcePath.set("test-resource.txt")
            }

            provider.isPresent.shouldBeTrue()
            provider.get() shouldBeEqual "Hello from a bundled resource\n"
        }

        test("StringResourceValueSource returns absent for missing resource") {
            val project = ProjectBuilder.builder().build()

            val provider = project.providers.of(StringResourceValueSource::class.java) {
                it.parameters.resourcePath.set("nonexistent-resource.txt")
            }

            provider.isPresent.shouldBeFalse()
        }

        test("PropertiesResourceValueSource returns properties") {
            val project = ProjectBuilder.builder().build()

            val provider = project.providers.of(PropertiesResourceValueSource::class.java) {
                it.parameters.resourcePath.set("test-resource.properties")
            }

            provider.isPresent.shouldBeTrue()
            provider.get().getProperty("greeting") shouldBeEqual "Hello"
            provider.get().getProperty("target") shouldBeEqual "World"
        }

        test("PropertiesResourceValueSource returns absent for missing resource") {
            val project = ProjectBuilder.builder().build()

            val provider = project.providers.of(PropertiesResourceValueSource::class.java) {
                it.parameters.resourcePath.set("nonexistent.properties")
            }

            provider.isPresent.shouldBeFalse()
        }
    }
}
