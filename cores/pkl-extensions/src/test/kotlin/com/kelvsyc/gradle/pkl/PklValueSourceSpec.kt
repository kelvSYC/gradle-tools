package com.kelvsyc.gradle.pkl

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class PklValueSourceSpec : FunSpec() {
    init {
        test("evaluates Pkl module from file") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile(suffix = ".pkl")
            baseFile.writeText(
                """
                name = "my-app"
                version = "1.0.0"
                """.trimIndent(),
            )
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.pklFile(file)
            val module = result.get()

            module.properties["name"] shouldBe "my-app"
            module.properties["version"] shouldBe "1.0.0"
        }

        test("returns absent for missing file") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            val file = project.layout.file(project.providers.provider { File(baseDir, "missing.pkl") })

            val result = project.providers.pklFile(file)

            result.orNull.shouldBeNull()
        }

        test("returns absent for invalid Pkl") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile(suffix = ".pkl")
            baseFile.writeText("not valid pkl {{{")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.pklFile(file)

            result.orNull.shouldBeNull()
        }
    }
}
