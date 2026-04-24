package com.kelvsyc.gradle.github.actions

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

class GitHubRepoArchiveActionSpec : FunSpec() {
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
        context("GitHubRepoArchiveAction - archive format") {
            test("tar.gz extension uses tarball endpoint") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitHubRepoArchiveAction.Parameters>()
                params.ghCommand.set("gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitHubRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf("api", "/repos/myowner/myrepo/tarball/main")

                tempFile.delete()
            }

            test("tgz extension uses tarball endpoint") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tgz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitHubRepoArchiveAction.Parameters>()
                params.ghCommand.set("gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitHubRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf("api", "/repos/myowner/myrepo/tarball/main")

                tempFile.delete()
            }

            test("zip extension uses zipball endpoint") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".zip").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitHubRepoArchiveAction.Parameters>()
                params.ghCommand.set("gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitHubRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf("api", "/repos/myowner/myrepo/zipball/main")

                tempFile.delete()
            }

            test("unsupported extension throws IllegalStateException") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.bz2").toFile()

                val params = project.objects.newInstance<GitHubRepoArchiveAction.Parameters>()
                params.ghCommand.set("gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                val action = object : GitHubRepoArchiveAction(mockk(relaxed = true), project.providers) {
                    override fun getParameters() = params
                }

                shouldThrow<IllegalStateException> { action.execute() }

                tempFile.delete()
            }
        }

        context("GitHubRepoArchiveAction - args structure") {
            test("ghCommand is used as the executable") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitHubRepoArchiveAction.Parameters>()
                params.ghCommand.set("/usr/local/bin/gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitHubRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                verify { execSpec.executable("/usr/local/bin/gh") }

                tempFile.delete()
            }
        }

        context("GitHubRepoArchiveAction - hostname") {
            test("no hostname - no --hostname flag in args") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitHubRepoArchiveAction.Parameters>()
                params.ghCommand.set("gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitHubRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--hostname") shouldBe false

                tempFile.delete()
            }

            test("with hostname - --hostname flag and value are appended") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitHubRepoArchiveAction.Parameters>()
                params.ghCommand.set("gh")
                params.hostname.set("github.example.com")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitHubRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf("api", "/repos/myowner/myrepo/tarball/main", "--hostname", "github.example.com")

                tempFile.delete()
            }
        }

        context("GitHubRepoArchiveAction - authentication") {
            test("token without hostname sets GH_TOKEN") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedEnv = captureEnv(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitHubRepoArchiveAction.Parameters>()
                params.ghCommand.set("gh")
                params.token.set("mytoken")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitHubRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedEnv shouldContain ("GH_TOKEN" to "mytoken")
                capturedEnv shouldNotContainKey "GH_ENTERPRISE_TOKEN"

                tempFile.delete()
            }

            test("token with hostname sets GH_ENTERPRISE_TOKEN") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedEnv = captureEnv(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitHubRepoArchiveAction.Parameters>()
                params.ghCommand.set("gh")
                params.hostname.set("github.example.com")
                params.token.set("mytoken")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitHubRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedEnv shouldContain ("GH_ENTERPRISE_TOKEN" to "mytoken")
                capturedEnv shouldNotContainKey "GH_TOKEN"

                tempFile.delete()
            }

            test("no token sets empty environment") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("archive", ".tar.gz").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedEnv = captureEnv(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<GitHubRepoArchiveAction.Parameters>()
                params.ghCommand.set("gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.ref.set("main")
                params.outputFile.set(tempFile)

                object : GitHubRepoArchiveAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedEnv shouldNotContainKey "GH_TOKEN"
                capturedEnv shouldNotContainKey "GH_ENTERPRISE_TOKEN"

                tempFile.delete()
            }
        }
    }
}
