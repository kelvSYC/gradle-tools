package com.kelvsyc.gradle.pkl

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class PklPathValueSourceSpec : FunSpec() {
    init {
        test("extracts string value by path") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile(suffix = ".pkl")
            baseFile.writeText(
                """
                name = "my-app"
                version = "1.0.0"
                """.trimIndent(),
            )
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.pklPath(file, "name")

            result.get() shouldBe "my-app"
        }

        test("extracts number as string") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile(suffix = ".pkl")
            baseFile.writeText("port = 8080")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.pklPath(file, "port")

            result.get() shouldBe "8080"
        }

        test("extracts boolean as string") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile(suffix = ".pkl")
            baseFile.writeText("debug = true")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.pklPath(file, "debug")

            result.get() shouldBe "true"
        }

        test("extracts nested value by dot-notation path") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile(suffix = ".pkl")
            baseFile.writeText(
                """
                database = new {
                    host = "localhost"
                    port = 5432
                }
                """.trimIndent(),
            )
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.pklPath(file, "database.host")

            result.get() shouldBe "localhost"
        }

        test("returns absent for missing path") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile(suffix = ".pkl")
            baseFile.writeText("name = \"my-app\"")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.pklPath(file, "missing")

            result.orNull.shouldBeNull()
        }

        test("returns absent for non-scalar value") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile(suffix = ".pkl")
            baseFile.writeText(
                """
                database = new {
                    host = "localhost"
                }
                """.trimIndent(),
            )
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.pklPath(file, "database")

            result.orNull.shouldBeNull()
        }

        test("returns absent for missing file") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            val file = project.layout.file(project.providers.provider { File(baseDir, "missing.pkl") })

            val result = project.providers.pklPath(file, "name")

            result.orNull.shouldBeNull()
        }

        test("returns absent for invalid Pkl") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile(suffix = ".pkl")
            baseFile.writeText("not valid pkl {{{")
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.pklPath(file, "name")

            result.orNull.shouldBeNull()
        }
    }
}
