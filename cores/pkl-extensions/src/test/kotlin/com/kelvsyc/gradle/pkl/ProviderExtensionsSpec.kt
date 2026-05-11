package com.kelvsyc.gradle.pkl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder

class ProviderExtensionsSpec : FunSpec() {
    init {
        test("parsePkl transforms string provider into PModule provider") {
            val project = ProjectBuilder.builder().build()
            val stringProvider = project.providers.provider { "name = \"my-app\"\nversion = \"1.0.0\"" }

            val pklProvider = stringProvider.parsePkl()
            val module = pklProvider.get()

            module.properties["name"] shouldBe "my-app"
            module.properties["version"] shouldBe "1.0.0"
        }
    }
}
