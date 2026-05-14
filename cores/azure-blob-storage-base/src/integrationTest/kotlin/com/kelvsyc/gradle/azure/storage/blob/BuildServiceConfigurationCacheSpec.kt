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
 * - `credential` (`Property<TokenCredential>`) is now exercised — this is expected to fail until
 *   the `credential` parameter shape is decomposed into serializable primitives (credential source
 *   enum + companion strings), with SDK credential objects reconstructed inside `createClient()`.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("BuildService with no parameter values survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "no-params",
            parametersBlock = ""
        )
    }

    test("BuildService with endpoint String property survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "endpoint-string",
            parametersBlock = "endpoint.set(\"https://example.blob.core.windows.net\")"
        )
    }

    test("BuildService with TokenCredential survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "token-credential",
            parametersBlock = "credential.set(TokenCredential { _ -> Mono.empty() })"
        )
    }

})

private fun assertParamsRoundTripCleanly(name: String, parametersBlock: String) {
    val projectDir = writeConfigCacheProbeProject(name, parametersBlock)

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
    val projectDir = IntegrationTestSupport.newProjectDir("blob-config-cache-probe-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.azure.storage.blob.BlobServiceClientBuildService
        import com.kelvsyc.gradle.azure.storage.blob.fixtures.BlobBuildServiceProbeTask
        import com.azure.core.credential.TokenCredential
        import reactor.core.publisher.Mono

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

