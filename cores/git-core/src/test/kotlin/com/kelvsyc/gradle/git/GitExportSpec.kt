package com.kelvsyc.gradle.git

import com.kelvsyc.gradle.plugins.GitCorePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class GitExportSpec : FunSpec() {
    init {
        context("GitExport - property configuration") {
            test("remoteUrl can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GitExport>("myTask").get()

                task.remoteUrl.set("https://example.com/repo.git")

                task.remoteUrl.get() shouldBe "https://example.com/repo.git"
            }

            test("ref can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GitExport>("myTask").get()

                task.ref.set("main")

                task.ref.get() shouldBe "main"
            }

            test("outputDirectory can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("export").toFile()
                val task = project.tasks.register<GitExport>("myTask").get()

                task.outputDirectory.set(tempDir)

                task.outputDirectory.get().asFile shouldBe tempDir

                tempDir.deleteRecursively()
            }

            test("paths can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GitExport>("myTask").get()

                task.paths.addAll(listOf("src/", "README.md"))

                task.paths.get() shouldBe listOf("src/", "README.md")
            }

            test("paths default to empty list") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GitExport>("myTask").get()

                task.paths.get() shouldBe emptyList()
            }

            test("gitCommand can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GitExport>("myTask").get()

                task.gitCommand.set("/usr/bin/git")

                task.gitCommand.get() shouldBe "/usr/bin/git"
            }

            test("verbose can be set to true") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GitExport>("myTask").get()

                task.verbose.set(true)

                task.verbose.get() shouldBe true
            }

            test("verbose can be set to false") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GitExport>("myTask").get()

                task.verbose.set(false)

                task.verbose.get() shouldBe false
            }
        }

        context("GitExport - plugin convention") {
            test("gitCommand is absent without plugin applied") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<GitExport>("myTask").get()

                task.gitCommand.isPresent shouldBe false
            }

            test("gitCommand has a convention after GitCorePlugin is applied") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(GitCorePlugin::class)

                val task = project.tasks.register<GitExport>("myTask").get()

                task.gitCommand.isPresent shouldBe true
            }

            test("explicit gitCommand overrides the convention set by the plugin") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(GitCorePlugin::class)

                val task = project.tasks.register<GitExport>("myTask") {
                    gitCommand.set("/custom/git")
                }.get()

                task.gitCommand.get() shouldBe "/custom/git"
            }
        }
    }
}
