package com.kelvsyc.gradle.providers

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeFalse
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.equals.shouldBeEqual
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.property.Arb
import io.kotest.property.arbitrary.string
import io.kotest.property.checkAll
import org.gradle.testfixtures.ProjectBuilder

class ProviderFactoryExtensionsSpec : FunSpec() {
    init {
        test("ofNullable with non-null value") {
            val project = ProjectBuilder.builder().build()
            checkAll(Arb.string()) {
                val result = project.providers.ofNullable(it)

                result.isPresent.shouldBeTrue()
                result.get() shouldBeEqual it
            }
        }

        test("ofNullable with null value is absent") {
            val project = ProjectBuilder.builder().build()

            val result = project.providers.ofNullable(null as String?)

            result.isPresent.shouldBeFalse()
        }

        test("absent is always absent") {
            val project = ProjectBuilder.builder().build()

            val result = project.providers.absent

            result.isPresent.shouldBeFalse()
            result.orNull.shouldBeNull()
        }

        test("propertiesFile with RegularFile") {
            val project = ProjectBuilder.builder().build()
            val file = project.layout.projectDirectory.file("gradle.properties")

            val result = project.providers.propertiesFile(file)

            // Result is a valid provider (may be absent if file doesn't exist)
            result.orNull
        }

        test("propertiesFile with Provider of RegularFile") {
            val project = ProjectBuilder.builder().build()
            val fileProvider = project.layout.file(project.providers.provider {
                project.layout.projectDirectory.file("gradle.properties").asFile
            })

            val result = project.providers.propertiesFile(fileProvider)

            // Result is a valid provider (may be absent if file doesn't exist)
            result.orNull
        }
    }
}
