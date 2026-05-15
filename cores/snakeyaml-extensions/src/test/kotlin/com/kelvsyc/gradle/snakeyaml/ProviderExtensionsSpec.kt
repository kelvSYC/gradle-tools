package com.kelvsyc.gradle.snakeyaml

import com.kelvsyc.kotlin.snakeyaml.YamlMapping
import com.kelvsyc.kotlin.snakeyaml.YamlSequence
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testfixtures.ProjectBuilder

class ProviderExtensionsSpec : FunSpec() {
    private val yaml = """
        server:
          host: localhost
          port: "8080"
          debug: "true"
          ratio: "1.5"
        items:
          - alpha
          - beta
        nested:
          inner:
            value: deep
    """.trimIndent()

    init {
        test("Provider<String>.parseYaml() lazily parses YAML") {
            val project = ProjectBuilder.builder().build()
            val stringProvider = project.providers.provider { yaml }

            val yamlProvider = stringProvider.parseYaml()
            val value = yamlProvider.get()

            value.shouldBeInstanceOf<YamlMapping>()
        }

        test("stringAt returns string at path") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.stringAt("server", "host").get() shouldBe "localhost"
        }

        test("intAt returns integer at path") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.intAt("server", "port").get() shouldBe 8080
        }

        test("longAt returns long at path") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.longAt("server", "port").get() shouldBe 8080L
        }

        test("doubleAt returns double at path") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.doubleAt("server", "ratio").get() shouldBe 1.5
        }

        test("booleanAt returns boolean at path") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.booleanAt("server", "debug").get() shouldBe true
        }

        test("mappingAt returns mapping at path") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.mappingAt("server").get().shouldBeInstanceOf<YamlMapping>()
        }

        test("sequenceAt returns sequence at path") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.sequenceAt("items").get().shouldBeInstanceOf<YamlSequence>()
        }

        test("stringAt returns absent for missing path") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.stringAt("server", "missing").orNull.shouldBeNull()
        }

        test("intAt returns absent for non-integer scalar") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.intAt("server", "host").orNull.shouldBeNull()
        }

        test("stringAt returns absent for non-scalar path") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.stringAt("server").orNull.shouldBeNull()
        }

        test("navigates multi-level nested path") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.stringAt("nested", "inner", "value").get() shouldBe "deep"
        }

        test("sequenceAt supports index navigation") {
            val project = ProjectBuilder.builder().build()
            val provider = project.providers.provider { yaml }.parseYaml()

            provider.stringAt("items", "0").get() shouldBe "alpha"
        }
    }
}
