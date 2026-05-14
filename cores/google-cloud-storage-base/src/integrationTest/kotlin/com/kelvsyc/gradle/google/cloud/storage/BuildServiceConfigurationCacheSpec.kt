package com.kelvsyc.gradle.google.cloud.storage

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe #1: configuration-cache round-trip of `StorageClientBuildService.Params`.
 *
 * Tests encode the **observed** current behavior. A failing test means the underlying Gradle/SDK
 * behavior has shifted — investigate before "fixing" the test.
 *
 * ### Findings (as of the integration-test introduction)
 *
 * - `projectId` (`Property<String>`) round-trips cleanly — `String` is natively `Serializable`.
 * - `credentials` (`Property<Credentials>`) set to `NoCredentials.getInstance()` also round-trips. Unlike
 *   AWS SDK's `Region` and `StaticCredentialsProvider`, Google's auth library appears to use
 *   `Serializable`-friendly types in this case.
 * - `GoogleCredentials.create(AccessToken)` is now exercised — this is expected to fail until the
 *   `credentials` parameter shape is decomposed into serializable primitives (credential source enum
 *   + companion strings), with SDK credential objects reconstructed inside `createClient()`.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("BuildService with no parameter values survives config-cache round-trip") {
        assertParamsRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("BuildService with projectId String property survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "projectid",
            parametersBlock = "projectId.set(\"test-project\")"
        )
    }

    test("BuildService with NoCredentials credentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "no-credentials",
            parametersBlock = "credentials.set(NoCredentials.getInstance())"
        )
    }

    test("BuildService with GoogleCredentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "google-credentials",
            parametersBlock = """
                credentials.set(GoogleCredentials.create(AccessToken("fake-token", null)))
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
    val projectDir = IntegrationTestSupport.newProjectDir("gcs-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.google.auth.oauth2.AccessToken
        import com.google.auth.oauth2.GoogleCredentials
        import com.google.cloud.NoCredentials
        import com.kelvsyc.gradle.google.cloud.storage.StorageClientBuildService
        import com.kelvsyc.gradle.google.cloud.storage.fixtures.StorageBuildServiceProbeTask

        val storageService = gradle.sharedServices.registerIfAbsent(
            "storage",
            StorageClientBuildService::class
        ) {
            parameters {
                $parametersBlock
            }
        }

        tasks.register<StorageBuildServiceProbeTask>("probe") {
            service.set(storageService)
            usesService(storageService)
        }
        """.trimIndent()
    )
    return projectDir
}
