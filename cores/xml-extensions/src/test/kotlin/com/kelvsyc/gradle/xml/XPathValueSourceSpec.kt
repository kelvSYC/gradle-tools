package com.kelvsyc.gradle.xml

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class XPathValueSourceSpec : FunSpec() {
    init {
        test("extracts value via XPath expression") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""<project><version>1.0.0</version></project>""")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.xpath(file, "version")

            result.get() shouldBe "1.0.0"
        }

        test("extracts attribute value") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""<item id="42" name="widget"/>""")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.xpath(file, "@id")

            result.get() shouldBe "42"
        }

        test("extracts descendant value") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""
                <project>
                    <dependencies>
                        <dependency><groupId>com.example</groupId></dependency>
                    </dependencies>
                </project>
            """.trimIndent())
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.xpath(file, "dependencies/dependency/groupId")

            result.get() shouldBe "com.example"
        }

        test("returns absent for non-matching path") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""<project><name>my-app</name></project>""")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.xpath(file, "nonexistent")

            result.orNull.shouldBeNull()
        }

        test("returns absent for multiple matches") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""<root><item>a</item><item>b</item></root>""")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.xpath(file, "item")

            result.orNull.shouldBeNull()
        }

        test("returns absent for missing file") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            val file = project.layout.file(project.providers.provider { File(baseDir, "missing.xml") })

            val result = project.providers.xpath(file, "version")

            result.orNull.shouldBeNull()
        }

        test("returns absent for invalid XML") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("not valid xml <<<")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.xpath(file, "version")

            result.orNull.shouldBeNull()
        }
    }
}
