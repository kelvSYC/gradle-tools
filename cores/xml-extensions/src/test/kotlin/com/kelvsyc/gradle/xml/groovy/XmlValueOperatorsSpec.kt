package com.kelvsyc.gradle.xml.groovy

import com.kelvsyc.kotlin.xml.parseXml
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.nulls.shouldNotBeNull
import io.kotest.matchers.shouldBe

class XmlValueOperatorsSpec : FunSpec() {
    init {
        test("get by name returns first matching child element") {
            val root = "<root><child>value</child></root>".parseXml()

            val child = root["child"]

            child.shouldNotBeNull()
            child.stringValue shouldBe "value"
        }

        test("get by name returns null for missing child") {
            val root = "<root><child/></root>".parseXml()

            root["missing"].shouldBeNull()
        }

        test("get by index returns child element at position") {
            val root = """
                <root>
                    <item>first</item>
                    <item>second</item>
                    <item>third</item>
                </root>
            """.trimIndent().parseXml()

            root[0].shouldNotBeNull().stringValue shouldBe "first"
            root[1].shouldNotBeNull().stringValue shouldBe "second"
            root[2].shouldNotBeNull().stringValue shouldBe "third"
        }

        test("get by index returns null for out of bounds") {
            val root = "<root><item/></root>".parseXml()

            root[5].shouldBeNull()
        }

        test("chained navigation mimics GPathResult") {
            val xml = """
                <project>
                    <dependencies>
                        <dependency>
                            <groupId>com.example</groupId>
                        </dependency>
                    </dependencies>
                </project>
            """.trimIndent().parseXml()

            val groupId = xml["dependencies"]?.get("dependency")?.get("groupId")

            groupId.shouldNotBeNull()
            groupId.stringValue shouldBe "com.example"
        }
    }
}
