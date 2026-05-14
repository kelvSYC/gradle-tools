package com.kelvsyc.gradle.artifactory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `ArtifactoryClientBuildService.Params`.
 *
 * All parameters are `Property<String>` (`url`, `username`, `password`) so the CC codec trivially handles
 * them. Each test verifies that a first invocation stores the configuration cache entry and a second
 * invocation reuses it without re-executing task configuration.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("ArtifactoryClientBuildService with no parameters survives config-cache round-trip") {
        assertParamsRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("ArtifactoryClientBuildService with url-only survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "url-only",
            parametersBlock = """url.set("https://example.jfrog.io/artifactory")"""
        )
    }

    test("ArtifactoryClientBuildService with url and credentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "url-and-credentials",
            parametersBlock = """
                url.set("https://example.jfrog.io/artifactory")
                username.set("user")
                password.set("s3cr3t")
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
    val projectDir = IntegrationTestSupport.newProjectDir("artifactory-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.artifactory.ArtifactoryClientBuildService
        import com.kelvsyc.gradle.artifactory.fixtures.ArtifactoryClientBuildServiceProbeTask

        val artService = gradle.sharedServices.registerIfAbsent(
            "artifactory",
            ArtifactoryClientBuildService::class
        ) {
            parameters {
                $parametersBlock
            }
        }

        tasks.register<ArtifactoryClientBuildServiceProbeTask>("probe") {
            service.set(artService)
            usesService(artService)
        }
        """.trimIndent()
    )
    return projectDir
}
