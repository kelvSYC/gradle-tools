package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.nulls.shouldBeNull
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class PropertiesFromFileValueSourceSpec : FunSpec() {
    init {
        test("empty file") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            val file = project.layout.file(project.providers.ofNullable(baseFile))

            val result = project.providers.propertiesFile(file)
            val resultProperties = result.get()

            resultProperties.shouldBeEmpty()
        }

        test("single property") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("foo=bar")
            val file = project.layout.file(project.providers.ofNullable(baseFile))

            val result = project.providers.propertiesFile(file)
            val resultProperties = result.get()

            resultProperties.shouldHaveSize(1)
            resultProperties.getProperty("foo") shouldBeEqual "bar"
        }

        test("malformed unicode") {
            val project = ProjectBuilder.builder().build()
            val baseFile = tempfile()
            baseFile.writeText("\\ufoo")
            val file = project.layout.file(project.providers.ofNullable(baseFile))

            val result = project.providers.propertiesFile(file)
            val resultProperties = result.orNull

            resultProperties.shouldBeNull()
        }

        test("file not found") {
            val project = ProjectBuilder.builder().build()
            val baseDir = tempdir()
            val file = project.layout.file(project.providers.ofNullable(File(baseDir, "foo.properties")))

            val result = project.providers.propertiesFile(file)
            val resultProperties = result.orNull

            resultProperties.shouldBeNull()
        }
    }
}
