package com.kelvsyc.gradle.jfrog.actions

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

class PublishBuildInfoActionSpec : FunSpec() {
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
        context("PublishBuildInfoAction - args structure") {
            test("basic args: rt, build-publish, --url, --access-token, build name, build number") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<PublishBuildInfoAction.Parameters>()
                params.jfCommand.set("jf")
                params.serverUrl.set("https://artifactory.example.com")
                params.accessToken.set("mytoken")
                params.buildName.set("my-build")
                params.buildNumber.set("42")

                object : PublishBuildInfoAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "rt", "build-publish",
                    "--url", "https://artifactory.example.com",
                    "--access-token", "mytoken",
                    "my-build", "42"
                )
            }

            test("jfCommand is used as the executable") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<PublishBuildInfoAction.Parameters>()
                params.jfCommand.set("/usr/local/bin/jf")
                params.serverUrl.set("https://artifactory.example.com")
                params.accessToken.set("mytoken")
                params.buildName.set("my-build")
                params.buildNumber.set("42")

                object : PublishBuildInfoAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                verify { execSpec.executable("/usr/local/bin/jf") }
            }
        }

        context("PublishBuildInfoAction - env exclude patterns") {
            test("no env exclude patterns - no --env-exclude flag in args") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<PublishBuildInfoAction.Parameters>()
                params.jfCommand.set("jf")
                params.serverUrl.set("https://artifactory.example.com")
                params.accessToken.set("mytoken")
                params.buildName.set("my-build")
                params.buildNumber.set("42")

                object : PublishBuildInfoAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--env-exclude") shouldBe false
            }

            test("with env exclude patterns - --env-exclude flag with semicolon-joined value") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<PublishBuildInfoAction.Parameters>()
                params.jfCommand.set("jf")
                params.serverUrl.set("https://artifactory.example.com")
                params.accessToken.set("mytoken")
                params.buildName.set("my-build")
                params.buildNumber.set("42")
                params.envExcludePatterns.addAll(listOf("*SECRET*", "*PASSWORD*"))

                object : PublishBuildInfoAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "rt", "build-publish",
                    "--url", "https://artifactory.example.com",
                    "--access-token", "mytoken",
                    "--env-exclude", "*SECRET*;*PASSWORD*",
                    "my-build", "42"
                )
            }
        }
    }
}
