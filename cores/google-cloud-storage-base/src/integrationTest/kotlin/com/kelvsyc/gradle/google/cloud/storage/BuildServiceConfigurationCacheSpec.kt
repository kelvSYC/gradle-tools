package com.kelvsyc.gradle.google.cloud.storage

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe #1: configuration-cache round-trip of `StorageClientBuildService.Params`
 * (a.k.a. `GcpBuildServiceParams`).
 *
 * Each test pins down the **observed** behavior of the parameter shape — `projectId: Property<String>`,
 * `credentialSource: Property<GcpCredentialSource>`, `credentialsFile: RegularFileProperty`,
 * `credentialsJsonRef: Property<CredentialReference>`, `accessTokenRef: Property<CredentialReference>`. A failing test means either
 * Gradle's config-cache serializer or the Google Cloud extensions have regressed; investigate before
 * "fixing" the test.
 *
 * ### What this characterizes
 *
 * The Google Cloud SDK's `Credentials` (and `CredentialsProvider`) types are not, in the general
 * case, serializable by Gradle's configuration-cache codec — `GoogleCredentials.create(AccessToken(...))`
 * and `ServiceAccountCredentials.fromStream(...)` carry non-`Serializable` state. So
 * `GcpBuildServiceParams` exposes only serializable primitives and the SDK credential is reconstructed
 * inside `createClient()` via `resolveCredentials()` / `resolveCredentialsProvider()`. These tests
 * exercise every branch of [com.kelvsyc.gradle.google.cloud.GcpCredentialSource] (note:
 * `SERVICE_ACCOUNT_JSON_INLINE` was renamed to `SERVICE_ACCOUNT_JSON_ENV`) across the
 * configuration-cache boundary and a second invocation that should reuse the stored entry.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("BuildService with no parameter values survives config-cache round-trip") {
        assertParamsRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("BuildService with projectId String property survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "projectid",
            parametersBlock = """projectId.set("test-project")"""
        )
    }

    test("BuildService with NONE credentialSource survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "no-credentials",
            parametersBlock = "credentialSource.set(GcpCredentialSource.NONE)"
        )
    }

    test("BuildService with APPLICATION_DEFAULT credentialSource survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "application-default",
            parametersBlock = "credentialSource.set(GcpCredentialSource.APPLICATION_DEFAULT)"
        )
    }

    test("BuildService with ACCESS_TOKEN credentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "access-token",
            parametersBlock = """
                credentialSource.set(GcpCredentialSource.ACCESS_TOKEN)
                accessTokenRef.set(CredentialReference.EnvironmentVariable("GOOGLE_OAUTH2_TOKEN"))
            """.trimIndent()
        )
    }

    test("BuildService with SERVICE_ACCOUNT_JSON_INLINE credentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "service-account-inline",
            parametersBlock = """
                credentialSource.set(GcpCredentialSource.SERVICE_ACCOUNT_JSON_ENV)
                credentialsJsonRef.set(CredentialReference.EnvironmentVariable("GOOGLE_APPLICATION_CREDENTIALS_JSON"))
            """.trimIndent()
        )
    }

    test("BuildService with SERVICE_ACCOUNT_JSON_FILE credentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "service-account-file",
            parametersBlock = """
                credentialSource.set(GcpCredentialSource.SERVICE_ACCOUNT_JSON_FILE)
                credentialsFile.set(layout.projectDirectory.file("service-account.json"))
            """.trimIndent()
        )
    }

    test("BuildService with projectId and ACCESS_TOKEN credentials together survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "project-and-token",
            parametersBlock = """
                projectId.set("test-project")
                credentialSource.set(GcpCredentialSource.ACCESS_TOKEN)
                accessTokenRef.set(CredentialReference.EnvironmentVariable("GOOGLE_OAUTH2_TOKEN"))
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

        import com.kelvsyc.gradle.google.cloud.GcpCredentialSource
        import com.kelvsyc.gradle.google.cloud.storage.StorageClientBuildService
        import com.kelvsyc.gradle.google.cloud.storage.fixtures.StorageBuildServiceProbeTask
        import com.kelvsyc.gradle.clients.CredentialReference

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
