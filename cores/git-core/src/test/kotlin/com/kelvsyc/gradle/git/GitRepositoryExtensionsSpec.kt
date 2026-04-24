package com.kelvsyc.gradle.git

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.eclipse.jgit.api.Git
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class GitRepositoryExtensionsSpec : FunSpec() {
    init {
        context("Directory.asRepository") {
            test("returns a Repository for a valid git working tree") {
                val project = ProjectBuilder.builder().build()
                val tmpDir = Files.createTempDirectory("git-repo").toFile()

                Git.init().setDirectory(tmpDir).call().close()

                val dirProperty = project.objects.directoryProperty()
                dirProperty.set(tmpDir)

                val repo = dirProperty.get().asRepository

                repo shouldNotBe null
                repo?.workTree shouldBe tmpDir

                repo?.close()
                tmpDir.deleteRecursively()
            }

            test("returns null for a directory that is not a git working tree") {
                val project = ProjectBuilder.builder().build()
                val tmpDir = Files.createTempDirectory("non-git").toFile()

                val dirProperty = project.objects.directoryProperty()
                dirProperty.set(tmpDir)

                dirProperty.get().asRepository shouldBe null

                tmpDir.deleteRecursively()
            }
        }

        context("Provider<Directory>.asRepository") {
            test("provider is present for a valid git working tree") {
                val project = ProjectBuilder.builder().build()
                val tmpDir = Files.createTempDirectory("git-repo-provider").toFile()

                Git.init().setDirectory(tmpDir).call().close()

                val dirProperty = project.objects.directoryProperty()
                dirProperty.set(tmpDir)

                val repoProvider = dirProperty.asRepository

                repoProvider.isPresent shouldBe true
                repoProvider.get()?.workTree shouldBe tmpDir

                repoProvider.get()?.close()
                tmpDir.deleteRecursively()
            }

            test("provider is absent for a directory that is not a git working tree") {
                val project = ProjectBuilder.builder().build()
                val tmpDir = Files.createTempDirectory("non-git-provider").toFile()

                val dirProperty = project.objects.directoryProperty()
                dirProperty.set(tmpDir)

                dirProperty.asRepository.isPresent shouldBe false

                tmpDir.deleteRecursively()
            }
        }
    }
}
