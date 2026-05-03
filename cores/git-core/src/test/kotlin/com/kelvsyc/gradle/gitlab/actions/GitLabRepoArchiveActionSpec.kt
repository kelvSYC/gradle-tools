package com.kelvsyc.gradle.gitlab.actions

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContain
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.Action
import org.gradle.kotlin.dsl.newInstance
import org.gradle.process.ExecOperations
import org.gradle.process.ExecSpec
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class GitLabRepoArchiveActionSpec : FunSpec() {
    private fun captureArgs(execSpec: ExecSpec): MutableList<Any?> {
        val captured = mutableListOf<Any?>()
        every { execSpec.args(any<Iterable<*>>()) } answers {
            @Suppress("UNCHECKED_CAST")
            captured.addAll(firstArg<Iterable<*>>().toList())
            execSpec
        }
        return captured
    }

    private fun captureEnv(execSpec: ExecSpec): MutableMap<String, Any> {
        val captured = mutableMapOf<String, Any>()
        every { execSpec.environment(any()) } answers {
            @Suppress("UNCHECKED_CAST")
            captured.putAll(firstArg<Map<String, Any>>())
            execSpec
        }
        return captured
    }

    private fun mockExecOps(execSpec: ExecSpec): ExecOperations {
        val execOps = mockk<ExecOperations>()
        every { execOps.exec(any()) } answers {
            firstArg<Action<ExecSpec>>().execute(execSpec)
            mockk(relaxed = true)
        }
        return execOps
    }

    init {
        context("GitLabRepoArchiveAction - archive format") {
            test("tar.gz extension produces --format tar.gz") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitLabRepoArchiveAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitLabRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "repo", "archive",
                    "--repo", "myowner/myrepo",
                    "--sha", "main",
                    "--format", "tar.gz",
                    "--output", tempFile.absolutePath
                )

                tempFile.delete()
            }

            test("tgz extension produces --format tar.gz") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tgz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitLabRepoArchiveAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitLabRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "repo", "archive",
                    "--repo", "myowner/myrepo",
                    "--sha", "main",
                    "--format", "tar.gz",
                    "--output", tempFile.absolutePath
                )

                tempFile.delete()
            }

            test("tar.bz2 extension produces --format tar.bz2") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.bz2").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitLabRepoArchiveAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitLabRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "repo", "archive",
                    "--repo", "myowner/myrepo",
                    "--sha", "main",
                    "--format", "tar.bz2",
                    "--output", tempFile.absolutePath
                )

                tempFile.delete()
            }

            test("zip extension produces --format zip") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".zip").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitLabRepoArchiveAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitLabRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "repo", "archive",
                    "--repo", "myowner/myrepo",
                    "--sha", "main",
                    "--format", "zip",
                    "--output", tempFile.absolutePath
                )

                tempFile.delete()
            }

            test("unsupported extension throws IllegalStateException") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".rar").toFile()

                val params = project.objects.newInstance<GitLabRepoArchiveAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                val action = object : GitLabRepoArchiveAction(mockk(relaxed = true), project.providers) {
                    override fun getParameters() = params
                }

                shouldThrow<IllegalStateException> { action.execute() }

                tempFile.delete()
            }
        }

        context("GitLabRepoArchiveAction - args structure") {
            test("glabCommand is used as the executable") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitLabRepoArchiveAction.Parameters>()
                params.glabCommand.set("/usr/local/bin/glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitLabRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                verify { execSpec.executable("/usr/local/bin/glab") }

                tempFile.delete()
            }
        }

        context("GitLabRepoArchiveAction - hostname") {
            test("no hostname - repo arg is owner/repo") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitLabRepoArchiveAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitLabRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                val repoFlagIndex = capturedArgs.indexOf("--repo")
                capturedArgs[repoFlagIndex + 1] shouldBe "myowner/myrepo"

                tempFile.delete()
            }

            test("with hostname - repo arg is hostname/owner/repo") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitLabRepoArchiveAction.Parameters>()
                params.glabCommand.set("glab")
                params.hostname.set("gitlab.example.com")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitLabRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                val repoFlagIndex = capturedArgs.indexOf("--repo")
                capturedArgs[repoFlagIndex + 1] shouldBe "gitlab.example.com/myowner/myrepo"

                tempFile.delete()
            }
        }

        context("GitLabRepoArchiveAction - authentication") {
            test("token set - GITLAB_TOKEN env var is populated") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedEnv = captureEnv(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitLabRepoArchiveAction.Parameters>()
                params.glabCommand.set("glab")
                params.token.set("mytoken")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitLabRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedEnv shouldContain ("GITLAB_TOKEN" to "mytoken")

                tempFile.delete()
            }

            test("no token - empty environment") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedEnv = captureEnv(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitLabRepoArchiveAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitLabRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedEnv shouldNotContainKey "GITLAB_TOKEN"

                tempFile.delete()
            }
        }
    }
}
