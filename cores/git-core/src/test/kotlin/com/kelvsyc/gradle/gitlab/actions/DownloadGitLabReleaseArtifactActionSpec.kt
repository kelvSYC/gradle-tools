package com.kelvsyc.gradle.gitlab.actions

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

class DownloadGitLabReleaseArtifactActionSpec : FunSpec() {
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
        context("DownloadGitLabReleaseArtifactAction - args structure") {
            test("basic args: release, download, tag, --repo owner/repo, --dir") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitLabReleaseArtifactAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitLabReleaseArtifactAction(execOps, project.providers) {
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

            test("glabCommand is used as the executable") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitLabReleaseArtifactAction.Parameters>()
                params.glabCommand.set("/usr/local/bin/glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitLabReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                verify { execSpec.executable("/usr/local/bin/glab") }

                tempDir.deleteRecursively()
            }
        }

        context("DownloadGitLabReleaseArtifactAction - hostname") {
            test("no hostname - repo arg is owner/repo") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitLabReleaseArtifactAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitLabReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                val repoFlagIndex = capturedArgs.indexOf("--repo")
                capturedArgs[repoFlagIndex + 1] shouldBe "myowner/myrepo"

                tempDir.deleteRecursively()
            }

            test("with hostname - repo arg is hostname/owner/repo") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitLabReleaseArtifactAction.Parameters>()
                params.glabCommand.set("glab")
                params.hostname.set("gitlab.example.com")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitLabReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                val repoFlagIndex = capturedArgs.indexOf("--repo")
                capturedArgs[repoFlagIndex + 1] shouldBe "gitlab.example.com/myowner/myrepo"

                tempDir.deleteRecursively()
            }
        }

        context("DownloadGitLabReleaseArtifactAction - asset names") {
            test("no asset names - no --asset-name flags in args") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitLabReleaseArtifactAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitLabReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--asset-name") shouldBe false

                tempDir.deleteRecursively()
            }

            test("with asset names - --asset-name flags appear for each name before --dir") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitLabReleaseArtifactAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.assetNames.addAll(listOf("*.zip", "*.tar.gz"))
                params.outputDirectory.set(tempDir)

                object : DownloadGitLabReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "release",
                    "download",
                    "v1.0.0",
                    "--repo",
                    "myowner/myrepo",
                    "--asset-name",
                    "*.zip",
                    "--asset-name",
                    "*.tar.gz",
                    "--dir",
                    tempDir.absolutePath
                )

                tempDir.deleteRecursively()
            }
        }

        context("DownloadGitLabReleaseArtifactAction - authentication") {
            test("token set - GITLAB_TOKEN env var is populated") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedEnv = captureEnv(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitLabReleaseArtifactAction.Parameters>()
                params.glabCommand.set("glab")
                params.token.set("mytoken")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitLabReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedEnv shouldContain ("GITLAB_TOKEN" to "mytoken")

                tempDir.deleteRecursively()
            }

            test("no token - empty environment") {
                val project = ProjectBuilder.builder().build()
                val tempDir = Files.createTempDirectory("release").toFile()

                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedEnv = captureEnv(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<DownloadGitLabReleaseArtifactAction.Parameters>()
                params.glabCommand.set("glab")
                params.owner.set("myowner")
                params.repo.set("myrepo")
                params.tag.set("v1.0.0")
                params.outputDirectory.set(tempDir)

                object : DownloadGitLabReleaseArtifactAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedEnv shouldNotContainKey "GITLAB_TOKEN"

                tempDir.deleteRecursively()
            }
        }
    }
}
