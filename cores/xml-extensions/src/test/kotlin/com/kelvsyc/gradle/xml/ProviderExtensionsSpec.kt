package com.kelvsyc.gradle.xml

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder

class ProviderExtensionsSpec : FunSpec() {
    init {
        test("parseXml transforms string provider into XmlElement provider") {
            val project = ProjectBuilder.builder().build()
            val stringProvider = project.providers.provider { "<root><child>value</child></root>" }

            val xmlProvider = stringProvider.parseXml()
            val root = xmlProvider.get()

            root.name.localPart shouldBe "root"
            root.element("child")!!.stringValue shouldBe "value"
        }
    }
}
