package com.kelvsyc.gradle.moshi

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class JsonPathValueSourceSpec : FunSpec() {
    init {
        test("extracts string value by path") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""{"user": {"name": "Bob"}}""")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.jsonPath(file, "$.user.name")

            result.get() shouldBe "Bob"
        }

        test("extracts number as string") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""{"version": 42}""")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.jsonPath(file, "$.version")

            result.get() shouldBe "42.0"
        }

        test("extracts boolean as string") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""{"enabled": true}""")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.jsonPath(file, "$.enabled")

            result.get() shouldBe "true"
        }

        test("returns absent for missing path") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""{"name": "Alice"}""")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.jsonPath(file, "$.missing")

            result.orNull.shouldBeNull()
        }

        test("returns absent for missing file") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            val file = project.layout.file(project.providers.provider { File(baseDir, "missing.json") })

            val result = project.providers.jsonPath(file, "$.name")

            result.orNull.shouldBeNull()
        }

        test("returns absent for null value") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""{"value": null}""")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.jsonPath(file, "$.value")

            result.orNull.shouldBeNull()
        }
    }
}
