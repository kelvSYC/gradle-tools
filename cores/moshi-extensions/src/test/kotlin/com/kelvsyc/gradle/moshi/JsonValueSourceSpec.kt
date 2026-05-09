package com.kelvsyc.gradle.moshi

import com.kelvsyc.kotlin.moshi.JsonObject
import com.kelvsyc.kotlin.moshi.JsonString
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class JsonValueSourceSpec : FunSpec() {
    init {
        test("parses JSON object from file") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("""{"name": "Alice", "age": 30}""")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.jsonFile(file)
            val value = result.get()

            value.shouldBeInstanceOf<JsonObject>()
            value["name"] shouldBe JsonString("Alice")
        }

        test("returns absent for missing file") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            val file = project.layout.file(project.providers.provider { File(baseDir, "missing.json") })

            val result = project.providers.jsonFile(file)

            result.orNull.shouldBeNull()
        }

        test("returns absent for invalid JSON") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("not valid json {{{")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.jsonFile(file)

            result.orNull.shouldBeNull()
        }
    }
}
