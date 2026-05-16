package com.kelvsyc.gradle.karakum.actions

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

class RunKarakumActionSpec : FunSpec() {
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
        context("RunKarakumAction - direct input mode") {
            test("uses first command token as executable") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<RunKarakumAction.Parameters>()
                params.karakumCommand.set(listOf("karakum"))
                params.inputFiles.from(project.file("src/types/index.d.ts"))
                params.outputDirectory.set("build/generated/karakum")

                object : RunKarakumAction(execOps) {
                    override fun getParameters() = params
                }.execute()

                verify { execSpec.executable("karakum") }
            }

            test("npx prefix tokens are passed as leading args before --input/--output") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<RunKarakumAction.Parameters>()
                params.karakumCommand.set(listOf("npx", "karakum@1.2.3"))
                params.inputFiles.from(project.file("types/lib.d.ts"))
                params.outputDirectory.set("build/generated/karakum")

                object : RunKarakumAction(execOps) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.first() shouldBe "karakum@1.2.3"
                verify { execSpec.executable("npx") }
            }

            test("emits --input for each file and --output for the output directory") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val inputFile = project.file("src/types/index.d.ts")
                val outDir = "build/generated/karakum"

                val params = project.objects.newInstance<RunKarakumAction.Parameters>()
                params.karakumCommand.set(listOf("karakum"))
                params.inputFiles.from(inputFile)
                params.outputDirectory.set(outDir)

                object : RunKarakumAction(execOps) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf("--input", inputFile.absolutePath, "--output", outDir)
            }

            test("emits --input for each file when multiple inputs provided") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val file1 = project.file("types/a.d.ts")
                val file2 = project.file("types/b.d.ts")

                val params = project.objects.newInstance<RunKarakumAction.Parameters>()
                params.karakumCommand.set(listOf("karakum"))
                params.inputFiles.from(file1, file2)
                params.outputDirectory.set("build/generated/karakum")

                object : RunKarakumAction(execOps) {
                    override fun getParameters() = params
                }.execute()

                val inputArgs = capturedArgs.chunked(2).filter { it.first() == "--input" }
                inputArgs.map { it[1] } shouldBe listOf(file1.absolutePath, file2.absolutePath)
            }
        }

        context("RunKarakumAction - config file mode") {
            test("emits --config with the config file path when configFile is set") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val configPath = "/project/karakum.config.json"

                val params = project.objects.newInstance<RunKarakumAction.Parameters>()
                params.karakumCommand.set(listOf("karakum"))
                params.configFile.set(configPath)

                object : RunKarakumAction(execOps) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs shouldBe listOf("--config", configPath)
            }

            test("config file mode does not emit --input or --output") {
                val project = ProjectBuilder.builder().build()
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val capturedArgs = captureArgs(execSpec)
                val execOps = mockExecOps(execSpec)

                val params = project.objects.newInstance<RunKarakumAction.Parameters>()
                params.karakumCommand.set(listOf("karakum"))
                params.configFile.set("/project/karakum.config.json")

                object : RunKarakumAction(execOps) {
                    override fun getParameters() = params
                }.execute()

                capturedArgs.contains("--input") shouldBe false
                capturedArgs.contains("--output") shouldBe false
            }
        }
    }
}
