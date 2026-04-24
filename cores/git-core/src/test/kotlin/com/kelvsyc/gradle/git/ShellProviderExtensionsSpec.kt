package com.kelvsyc.gradle.git

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.Action
import org.gradle.api.provider.ProviderFactory
import org.gradle.process.ExecOutput
import org.gradle.process.ExecSpec

class ShellProviderExtensionsSpec : FunSpec() {
    private fun mockProviderFactory(execSpec: ExecSpec): ProviderFactory {
        val providers = mockk<ProviderFactory>()
        every { providers.exec(any()) } answers {
            firstArg<Action<ExecSpec>>().execute(execSpec)
            mockk<ExecOutput>(relaxed = true)
        }
        return providers
    }

    init {
        context("which - exec spec configuration") {
            test("uses 'which' as the executable") {
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val providers = mockProviderFactory(execSpec)

                providers.which("git")

                verify { execSpec.executable("which") }
            }

            test("passes the command name as args") {
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val providers = mockProviderFactory(execSpec)

                providers.which("git")

                verify { execSpec.args(listOf("git")) }
            }

            test("passes the correct command name when looking up a different command") {
                val execSpec = mockk<ExecSpec>(relaxed = true)
                val providers = mockProviderFactory(execSpec)

                providers.which("gh")

                verify { execSpec.args(listOf("gh")) }
            }
        }
    }
}
