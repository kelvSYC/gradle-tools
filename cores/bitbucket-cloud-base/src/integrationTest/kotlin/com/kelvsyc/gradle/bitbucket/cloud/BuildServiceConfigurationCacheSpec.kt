package com.kelvsyc.gradle.bitbucket.cloud

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `BitbucketCloudClientBuildService.Params`.
 *
 * `baseUrl` and `username` are `Property<String>`; `passwordRef` is `Property<CredentialReference>` which stores
 * only the lookup key. All three types are Gradle config-cache serializable. Each test verifies that a first invocation stores the configuration cache entry and a second
 * invocation reuses it without re-executing task configuration.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("BitbucketCloudClientBuildService with no parameters survives config-cache round-trip") {
        assertParamsRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("BitbucketCloudClientBuildService with baseUrl-only survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "base-url-only",
            parametersBlock = """baseUrl.set("https://api.bitbucket.org/2.0/")"""
        )
    }

    test("BitbucketCloudClientBuildService with username and password survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "credentials",
            parametersBlock = """
                username.set("user")
                passwordRef.set(CredentialReference.EnvironmentVariable("BITBUCKET_APP_PASSWORD"))
            """.trimIndent()
        )
    }

    test("BitbucketCloudClientBuildService with all parameters survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "all-params",
            parametersBlock = """
                baseUrl.set("https://api.bitbucket.org/2.0/")
                username.set("user")
                passwordRef.set(CredentialReference.EnvironmentVariable("BITBUCKET_APP_PASSWORD"))
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
    val projectDir = IntegrationTestSupport.newProjectDir("bitbucket-cloud-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.bitbucket.cloud.BitbucketCloudClientBuildService
        import com.kelvsyc.gradle.bitbucket.cloud.fixtures.BitbucketCloudClientBuildServiceProbeTask
        import com.kelvsyc.gradle.clients.CredentialReference

        val bbService = gradle.sharedServices.registerIfAbsent(
            "bitbucketCloud",
            BitbucketCloudClientBuildService::class
        ) {
            parameters {
                $parametersBlock
            }
        }

        tasks.register<BitbucketCloudClientBuildServiceProbeTask>("probe") {
            service.set(bbService)
            usesService(bbService)
        }
        """.trimIndent()
    )
    return projectDir
}
