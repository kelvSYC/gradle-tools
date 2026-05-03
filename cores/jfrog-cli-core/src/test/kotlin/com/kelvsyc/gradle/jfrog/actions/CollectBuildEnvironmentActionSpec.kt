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

class CollectBuildEnvironmentActionSpec : FunSpec() {
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
        context("CollectBuildEnvironmentAction - args structure") {
            test("basic args without patterns: rt, build-collect-env, build name, build number") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<CollectBuildEnvironmentAction.Parameters>()
                params.jfCommand.set("jf")
                params.buildName.set("my-build")
                params.buildNumber.set("42")

                object : CollectBuildEnvironmentAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf("rt", "build-collect-env", "my-build", "42")
            }

            test("jfCommand is used as the executable") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<CollectBuildEnvironmentAction.Parameters>()
                params.jfCommand.set("/usr/local/bin/jf")
                params.buildName.set("my-build")
                params.buildNumber.set("42")

                object : CollectBuildEnvironmentAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                verify { execSpec.executable("/usr/local/bin/jf") }
            }
        }

        context("CollectBuildEnvironmentAction - include patterns") {
            test("no include patterns - no --include-vars flag in args") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<CollectBuildEnvironmentAction.Parameters>()
                params.jfCommand.set("jf")
                params.buildName.set("my-build")
                params.buildNumber.set("42")

                object : CollectBuildEnvironmentAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--include-vars") shouldBe false
            }

            test("with include patterns - --include-vars flag with semicolon-joined value") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<CollectBuildEnvironmentAction.Parameters>()
                params.jfCommand.set("jf")
                params.buildName.set("my-build")
                params.buildNumber.set("42")
                params.includePatterns.addAll(listOf("MY_APP_*", "CI_*"))

                object : CollectBuildEnvironmentAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf("rt", "build-collect-env", "--include-vars", "MY_APP_*;CI_*", "my-build", "42")
            }
        }

        context("CollectBuildEnvironmentAction - exclude patterns") {
            test("no exclude patterns - no --exclude-vars flag in args") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<CollectBuildEnvironmentAction.Parameters>()
                params.jfCommand.set("jf")
                params.buildName.set("my-build")
                params.buildNumber.set("42")

                object : CollectBuildEnvironmentAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--exclude-vars") shouldBe false
            }

            test("with exclude patterns - --exclude-vars flag with semicolon-joined value") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<CollectBuildEnvironmentAction.Parameters>()
                params.jfCommand.set("jf")
                params.buildName.set("my-build")
                params.buildNumber.set("42")
                params.excludePatterns.addAll(listOf("*SECRET*", "*PASSWORD*"))

                object : CollectBuildEnvironmentAction(execOps, project.providers) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf("rt", "build-collect-env", "--exclude-vars", "*SECRET*;*PASSWORD*", "my-build", "42")
            }
        }
    }
}
