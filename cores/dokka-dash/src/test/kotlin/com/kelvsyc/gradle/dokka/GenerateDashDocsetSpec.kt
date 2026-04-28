package com.kelvsyc.gradle.dokka

import com.kelvsyc.gradle.plugins.DokkaDashPlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class GenerateDashDocsetSpec : FunSpec() {
    init {
        context("GenerateDashDocset - property configuration") {
            test("docsetName can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GenerateDashDocset>("myTask").get()

                task.docsetName.set("MyLibrary")

                task.docsetName.get() shouldBe "MyLibrary"
            }

            test("bundleIdentifier can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GenerateDashDocset>("myTask").get()

                task.bundleIdentifier.set("com.example.mylibrary")

                task.bundleIdentifier.get() shouldBe "com.example.mylibrary"
            }

            test("indexPage can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GenerateDashDocset>("myTask").get()

                task.indexPage.set("index.html")

                task.indexPage.get() shouldBe "index.html"
            }

            test("indexPage is absent by default") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GenerateDashDocset>("myTask").get()

                task.indexPage.isPresent shouldBe false
            }

            test("dokkaOutputDirectory can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("dokka").toFile()
                val task = project.tasks.register<GenerateDashDocset>("myTask").get()

                task.dokkaOutputDirectory.set(tempDir)

                task.dokkaOutputDirectory.get().asFile shouldBe tempDir
                tempDir.deleteRecursively()
            }

            test("outputDirectory can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("output").toFile()
                val task = project.tasks.register<GenerateDashDocset>("myTask").get()

                task.outputDirectory.set(tempDir)

                task.outputDirectory.get().asFile shouldBe tempDir
                tempDir.deleteRecursively()
            }

            test("docsetDirectory is computed from outputDirectory and docsetName") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("output").toFile()
                val task = project.tasks.register<GenerateDashDocset>("myTask").get()

                task.outputDirectory.set(tempDir)
                task.docsetName.set("MyLibrary")

                task.docsetDirectory.get().asFile shouldBe tempDir.resolve("MyLibrary.docset")
                tempDir.deleteRecursively()
            }
        }

        context("GenerateDashDocset - plugin convention") {
            test("workerClasspath is empty without plugin applied") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GenerateDashDocset>("myTask").get()

                task.workerClasspath.from.isEmpty() shouldBe true
            }

            test("workerClasspath is configured after DokkaDashPlugin is applied") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(DokkaDashPlugin::class)
                val task = project.tasks.register<GenerateDashDocset>("myTask").get()

                task.workerClasspath.from.isEmpty() shouldBe false
            }
        }
    }
}
