package com.kelvsyc.gradle.azure.storage.blob

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe #1: configuration-cache round-trip of `BlobServiceClientBuildService.Params`.
 *
 * Tests encode the **observed** current behavior. A failing test means the underlying Gradle/SDK
 * behavior has shifted — investigate before "fixing" the test.
 *
 * ### Findings (as of the integration-test introduction)
 *
 * - `endpoint` (`Property<String>`) round-trips cleanly — `String` is natively `Serializable`.
 * - `credential` (`Property<TokenCredential>`) is not exercised here; instantiating a real
 *   `TokenCredential` in a TestKit project requires either Azure Identity dependencies or a custom
 *   implementation. Deferred to the v2 coverage expansion once we know what production deployments use.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("BuildService with no parameter values survives config-cache round-trip") {
        val projectDir = writeConfigCacheProbeProject(parametersBlock = "")

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

    test("BuildService with endpoint String property survives config-cache round-trip") {
        val projectDir = writeConfigCacheProbeProject(
            parametersBlock = "endpoint.set(\"https://example.blob.core.windows.net\")"
        )
        val outcome = IntegrationTestSupport.runProbe(
            projectDir, "probe", "--configuration-cache", "--stacktrace"
        )
        val succeeded = outcome.shouldBeInstanceOf<ProbeOutcome.Succeeded>()
        succeeded.result.task(":probe")?.outcome shouldBe TaskOutcome.SUCCESS
    }

})

private fun writeConfigCacheProbeProject(parametersBlock: String): File {
    val projectDir = IntegrationTestSupport.newProjectDir("blob-config-cache-probe")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.azure.storage.blob.BlobServiceClientBuildService
        import com.kelvsyc.gradle.azure.storage.blob.fixtures.BlobBuildServiceProbeTask

        val blobService = gradle.sharedServices.registerIfAbsent(
            "blob",
            BlobServiceClientBuildService::class
        ) {
            parameters {
                $parametersBlock
            }
        }

        tasks.register<BlobBuildServiceProbeTask>("probe") {
            service.set(blobService)
            usesService(blobService)
        }
        """.trimIndent()
    )
    return projectDir
}
