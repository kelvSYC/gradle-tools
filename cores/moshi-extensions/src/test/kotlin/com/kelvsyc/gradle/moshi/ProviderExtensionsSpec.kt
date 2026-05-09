package com.kelvsyc.gradle.moshi

import com.kelvsyc.kotlin.moshi.JsonObject
import com.kelvsyc.kotlin.moshi.JsonString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testfixtures.ProjectBuilder

class ProviderExtensionsSpec : FunSpec() {
    init {
        test("Provider<String>.parseJson() lazily parses JSON") {
            val project = ProjectBuilder.builder().build()
            val stringProvider = project.providers.provider { """{"key": "value"}""" }

            val jsonProvider = stringProvider.parseJson()
            val value = jsonProvider.get()

            value.shouldBeInstanceOf<JsonObject>()
            value["key"] shouldBe JsonString("value")
        }
    }
}
