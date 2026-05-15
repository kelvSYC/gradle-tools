package com.kelvsyc.gradle.snakeyaml

import com.kelvsyc.kotlin.snakeyaml.YamlMapping
import com.kelvsyc.kotlin.snakeyaml.YamlScalar
import com.kelvsyc.kotlin.snakeyaml.YamlSequence
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class YamlValueSourceSpec : FunSpec() {
    init {
        test("parses YAML mapping from file") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText(
                """
                name: Alice
                age: "30"
                """.trimIndent()
            )
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.yamlFile(file)
            val value = result.get()

            value.shouldBeInstanceOf<YamlMapping>()
            value["name"] shouldBe YamlScalar("Alice")
        }

        test("parses YAML sequence from file") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText(
                """
                - alpha
                - beta
                - gamma
                """.trimIndent()
            )
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.yamlFile(file)
            val value = result.get()

            value.shouldBeInstanceOf<YamlSequence>()
            value.shouldBeInstanceOf<YamlSequence>().elements[1] shouldBe YamlScalar("beta")
        }

        test("returns absent for missing file") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            val file = project.layout.file(project.providers.provider { File(baseDir, "missing.yaml") })

            val result = project.providers.yamlFile(file)

            result.orNull.shouldBeNull()
        }

        test("returns absent for invalid YAML") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText(
                """
                key: [unclosed
                """.trimIndent()
            )
            val file = project.layout.file(project.providers.provider { baseFile })

            val result = project.providers.yamlFile(file)

            result.orNull.shouldBeNull()
        }
    }
}
