package com.kelvsyc.gradle.xml

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class XmlValueSourceSpec : FunSpec() {
    init {
        test("parses XML element from file") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""<project><name>my-app</name></project>""")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.xmlFile(file)
            val value = result.get()

            value.name.localPart shouldBe "project"
            value.element("name")!!.stringValue shouldBe "my-app"
        }

        test("returns absent for missing file") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            val file = project.layout.file(project.providers.provider { File(baseDir, "missing.xml") })

            val result = project.providers.xmlFile(file)

            result.orNull.shouldBeNull()
        }

        test("returns absent for invalid XML") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("not valid xml <<<")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.xmlFile(file)

            result.orNull.shouldBeNull()
        }
    }
}
