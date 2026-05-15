package com.kelvsyc.gradle.bitbucket.server

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `BitbucketServerClientBuildService.Params`.
 *
 * `baseUrl` is `Property<String>`; `tokenRef` is `Property<CredentialReference>` which stores only the lookup
 * key. Both types are Gradle config-cache serializable.
 * This spec serves as a baseline regression sentinel: if any future change to the params shape introduces
 * a non-serializable type, at least one test here will go red.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("BitbucketServerClientBuildService with no parameters survives config-cache round-trip") {
        assertParamsRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("BitbucketServerClientBuildService with token-only survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "token-only",
            parametersBlock = """tokenRef.set(CredentialReference.EnvironmentVariable("BITBUCKET_TOKEN"))"""
        )
    }

    test("BitbucketServerClientBuildService with baseUrl and token survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "base-url-and-token",
            parametersBlock = """
                baseUrl.set("https://bitbucket.example.com/")
                tokenRef.set(CredentialReference.EnvironmentVariable("BITBUCKET_TOKEN"))
            """.trimIndent()
        )
    }
})

private fun assertParamsRoundTripCleanly(name: String, parametersBlock: String) {
    val projectDir = writeConfigCacheProbeProject(name = name, parametersBlock = parametersBlock)

    val first = IntegrationTestSupport.runProbe(
        projectDir, "probe", "--configuration-cache", "--stacktrace"
    )
    val firstSucceeded = first.shouldBeInstanceOf<ProbeOutcome.Succeeded>()
    firstSucceeded.result.task(":probe")?.outcome shouldBe TaskOutcome.SUCCESS

    val second = IntegrationTestSupport.runProbe(
        projectDir, "probe", "--configuration-cache", "--stacktrace"
    )
    val secondSucceeded = second.shouldBeInstanceOf<ProbeOutcome.Succeeded>()
    secondSucceeded.result.task(":probe")?.outcome shouldBe TaskOutcome.SUCCESS
    secondSucceeded.result.output shouldContain "Configuration cache entry reused"
}

private fun writeConfigCacheProbeProject(name: String, parametersBlock: String): File {
    val projectDir = IntegrationTestSupport.newProjectDir("bitbucket-server-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.bitbucket.server.BitbucketServerClientBuildService
        import com.kelvsyc.gradle.bitbucket.server.fixtures.BitbucketServerClientBuildServiceProbeTask
        import com.kelvsyc.gradle.clients.CredentialReference

        val bbService = gradle.sharedServices.registerIfAbsent(
            "bitbucketServer",
            BitbucketServerClientBuildService::class
        ) {
            parameters {
                $parametersBlock
            }
        }

        tasks.register<BitbucketServerClientBuildServiceProbeTask>("probe") {
            service.set(bbService)
            usesService(bbService)
        }
        """.trimIndent()
    )
    return projectDir
}
