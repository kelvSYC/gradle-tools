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

class ScanBuildActionSpec : FunSpec() {
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
        context("ScanBuildAction - args structure") {
            test("basic args: rt, build-scan, --url, --access-token, build name, build number") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<ScanBuildAction.Parameters>()
                params.jfCommand.set("jf")
                params.serverUrl.set("https://artifactory.example.com")
                params.accessToken.set("mytoken")
                params.buildName.set("my-build")
                params.buildNumber.set("42")

                object : ScanBuildAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "rt", "build-scan",
                    "--url", "https://artifactory.example.com",
                    "--access-token", "mytoken",
                    "my-build", "42"
                )
            }

            test("jfCommand is used as the executable") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<ScanBuildAction.Parameters>()
                params.jfCommand.set("/usr/local/bin/jf")
                params.serverUrl.set("https://artifactory.example.com")
                params.accessToken.set("mytoken")
                params.buildName.set("my-build")
                params.buildNumber.set("42")

                object : ScanBuildAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                verify { execSpec.executable("/usr/local/bin/jf") }
            }
        }

        context("ScanBuildAction - failBuild flag") {
            test("failBuild not set - no --fail flag in args") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<ScanBuildAction.Parameters>()
                params.jfCommand.set("jf")
                params.serverUrl.set("https://artifactory.example.com")
                params.accessToken.set("mytoken")
                params.buildName.set("my-build")
                params.buildNumber.set("42")

                object : ScanBuildAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--fail") shouldBe false
            }

            test("failBuild false - no --fail flag in args") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<ScanBuildAction.Parameters>()
                params.jfCommand.set("jf")
                params.serverUrl.set("https://artifactory.example.com")
                params.accessToken.set("mytoken")
                params.buildName.set("my-build")
                params.buildNumber.set("42")
                params.failBuild.set(false)

                object : ScanBuildAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--fail") shouldBe false
            }

            test("failBuild true - --fail flag appears before build name") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<ScanBuildAction.Parameters>()
                params.jfCommand.set("jf")
                params.serverUrl.set("https://artifactory.example.com")
                params.accessToken.set("mytoken")
                params.buildName.set("my-build")
                params.buildNumber.set("42")
                params.failBuild.set(true)

                object : ScanBuildAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf(
                    "rt", "build-scan",
                    "--url", "https://artifactory.example.com",
                    "--access-token", "mytoken",
                    "--fail",
                    "my-build", "42"
                )
            }
        }
    }
}
