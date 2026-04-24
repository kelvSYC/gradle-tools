package com.kelvsyc.gradle.git.actions

import io.kotest.core.spec.style.FunSpec
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

class GitRemoteArchiveActionSpec : FunSpec() {
    private fun captureArgs(execSpec: ExecSpec): MutableList<Any?> {
        val captured = mutableListOf<Any?>()
        every { execSpec.args(any<Iterable<*>>()) } answers {
            @Suppress("UNCHECKED_CAST")
            captured.addAll(firstArg<Iterable<*>>().toList())
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
        context("GitRemoteArchiveAction - args structure") {
            test("Basic args contain archive, --remote, --output, and ref") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".zip").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitRemoteArchiveAction.Parameters>()
                params.gitCommand.set("git")
                params.remoteUrl.set("https://example.com/repo.git")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitRemoteArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "archive",
                    "--remote=https://example.com/repo.git",
                    "--output=${tempFile.absolutePath}",
                    "main"
                )

                tempFile.delete()
            }

            test("gitCommand is used as the executable") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".zip").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitRemoteArchiveAction.Parameters>()
                params.gitCommand.set("/usr/bin/git")
                params.remoteUrl.set("https://example.com/repo.git")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitRemoteArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                verify { execSpec.executable("/usr/bin/git") }

                tempFile.delete()
            }
        }

        context("GitRemoteArchiveAction - verbose") {
            test("verbose true adds --verbose to args") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".zip").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitRemoteArchiveAction.Parameters>()
                params.gitCommand.set("git")
                params.remoteUrl.set("https://example.com/repo.git")
                params.ref.set("main")
                params.outputFile.set(tempFile)
                params.verbose.set(true)

                object : GitRemoteArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--verbose") shouldBe true

                tempFile.delete()
            }

            test("verbose false - --verbose not in args") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".zip").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitRemoteArchiveAction.Parameters>()
                params.gitCommand.set("git")
                params.remoteUrl.set("https://example.com/repo.git")
                params.ref.set("main")
                params.outputFile.set(tempFile)
                params.verbose.set(false)

                object : GitRemoteArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--verbose") shouldBe false

                tempFile.delete()
            }

            test("verbose not set - --verbose not in args") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".zip").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitRemoteArchiveAction.Parameters>()
                params.gitCommand.set("git")
                params.remoteUrl.set("https://example.com/repo.git")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitRemoteArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--verbose") shouldBe false

                tempFile.delete()
            }
        }

        context("GitRemoteArchiveAction - paths") {
            test("with paths - paths appended after ref") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".zip").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitRemoteArchiveAction.Parameters>()
                params.gitCommand.set("git")
                params.remoteUrl.set("https://example.com/repo.git")
                params.ref.set("main")
                params.outputFile.set(tempFile)
                params.paths.addAll(listOf("src/", "README.md"))

                object : GitRemoteArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "archive",
                    "--remote=https://example.com/repo.git",
                    "--output=${tempFile.absolutePath}",
                    "main",
                    "src/",
                    "README.md"
                )

                tempFile.delete()
            }

            test("no paths - only ref after output args") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".zip").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitRemoteArchiveAction.Parameters>()
                params.gitCommand.set("git")
                params.remoteUrl.set("https://example.com/repo.git")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitRemoteArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "archive",
                    "--remote=https://example.com/repo.git",
                    "--output=${tempFile.absolutePath}",
                    "main"
                )

                tempFile.delete()
            }
        }
    }
}
