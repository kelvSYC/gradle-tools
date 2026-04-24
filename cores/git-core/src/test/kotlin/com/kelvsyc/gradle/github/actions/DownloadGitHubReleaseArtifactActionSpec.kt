package com.kelvsyc.gradle.github.actions

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

class DownloadGitHubReleaseArtifactActionSpec : FunSpec() {
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
        context("DownloadGitHubReleaseArtifactAction - args structure") {
            test("Basic args: release, download, tag, --repo owner/repo, --dir") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitHubReleaseArtifactAction.Parameters>()
                params.ghCommand.set("gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitHubReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "release",
                    "download",
                    "v1.0.0",
                    "--repo",
                    "myowner/myrepo",
                    "--dir",
                    tempDir.absolutePath
                )

                tempDir.deleteRecursively()
            }

            test("ghCommand is used as the executable") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitHubReleaseArtifactAction.Parameters>()
                params.ghCommand.set("/usr/local/bin/gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitHubReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                verify { execSpec.executable("/usr/local/bin/gh") }

                tempDir.deleteRecursively()
            }
        }

        context("DownloadGitHubReleaseArtifactAction - hostname") {
            test("no hostname - no --hostname flag in args") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitHubReleaseArtifactAction.Parameters>()
                params.ghCommand.set("gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitHubReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--hostname") shouldBe false

                tempDir.deleteRecursively()
            }

            test("with hostname - --hostname flag and value appear after tag") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitHubReleaseArtifactAction.Parameters>()
                params.ghCommand.set("gh")
                params.hostname.set("github.example.com")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitHubReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "release",
                    "download",
                    "v1.0.0",
                    "--hostname",
                    "github.example.com",
                    "--repo",
                    "myowner/myrepo",
                    "--dir",
                    tempDir.absolutePath
                )

                tempDir.deleteRecursively()
            }
        }

        context("DownloadGitHubReleaseArtifactAction - pattern globs") {
            test("no pattern globs - no --pattern flags in args") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitHubReleaseArtifactAction.Parameters>()
                params.ghCommand.set("gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitHubReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--pattern") shouldBe false

                tempDir.deleteRecursively()
            }

            test("with pattern globs - --pattern flags appear for each glob before --dir") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitHubReleaseArtifactAction.Parameters>()
                params.ghCommand.set("gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.patternGlobs.addAll(listOf("*.zip", "*.tar.gz"))
                params.outputDirectory.set(tempDir)

                object : DownloadGitHubReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "release",
                    "download",
                    "v1.0.0",
                    "--repo",
                    "myowner/myrepo",
                    "--pattern",
                    "*.zip",
                    "--pattern",
                    "*.tar.gz",
                    "--dir",
                    tempDir.absolutePath
                )

                tempDir.deleteRecursively()
            }
        }

        context("DownloadGitHubReleaseArtifactAction - authentication") {
            test("token without hostname sets GH_TOKEN") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedEnv = captureEnv(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitHubReleaseArtifactAction.Parameters>()
                params.ghCommand.set("gh")
                params.token.set("mytoken")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitHubReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedEnv shouldContain ("GH_TOKEN" to "mytoken")
                capturedEnv shouldNotContainKey "GH_ENTERPRISE_TOKEN"

                tempDir.deleteRecursively()
            }

            test("token with hostname sets GH_ENTERPRISE_TOKEN") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedEnv = captureEnv(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitHubReleaseArtifactAction.Parameters>()
                params.ghCommand.set("gh")
                params.hostname.set("github.example.com")
                params.token.set("mytoken")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitHubReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedEnv shouldContain ("GH_ENTERPRISE_TOKEN" to "mytoken")
                capturedEnv shouldNotContainKey "GH_TOKEN"

                tempDir.deleteRecursively()
            }

            test("no token - empty environment") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedEnv = captureEnv(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitHubReleaseArtifactAction.Parameters>()
                params.ghCommand.set("gh")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitHubReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedEnv shouldNotContainKey "GH_TOKEN"
                capturedEnv shouldNotContainKey "GH_ENTERPRISE_TOKEN"

                tempDir.deleteRecursively()
            }
        }
    }
}
