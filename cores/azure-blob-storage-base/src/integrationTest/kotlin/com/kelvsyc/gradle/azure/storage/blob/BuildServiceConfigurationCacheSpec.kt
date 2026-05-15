package com.kelvsyc.gradle.azure.storage.blob

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe #1: configuration-cache round-trip of `BlobServiceClientBuildService.Params`
 * (a.k.a. `AzureBuildServiceParams`).
 *
 * Each test pins down the **observed** behavior of the parameter shape — `endpoint: Property<String>`,
 * `credentialSource: Property<AzureCredentialSource>`, plus the supporting credential strings on
 * `AzureBuildServiceParams`. A failing test means either Gradle's config-cache serializer or the
 * Azure extensions have regressed; investigate before "fixing" the test.
 *
 * ### What this characterizes
 *
 * Azure SDK `TokenCredential` implementations (`DefaultAzureCredential`, `ManagedIdentityCredential`,
 * `ClientSecretCredential`) carry non-`Serializable` state and cannot survive Gradle's
 * configuration-cache codec. `AzureBuildServiceParams` exposes only serializable primitives and the
 * SDK credential is reconstructed inside `createClient()` via `resolveCredential()` /
 * `resolveTokenCredential()`. These tests exercise every branch of
 * [com.kelvsyc.gradle.azure.AzureCredentialSource] across the configuration-cache boundary and a
 * second invocation that should reuse the stored entry.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("BuildService with no parameter values survives config-cache round-trip") {
        assertParamsRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("BuildService with endpoint String property survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "endpoint-string",
            parametersBlock = """endpoint.set("https://example.blob.core.windows.net")"""
        )
    }

    test("BuildService with NONE credentialSource survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "no-credentials",
            parametersBlock = "credentialSource.set(AzureCredentialSource.NONE)"
        )
    }

    test("BuildService with DEFAULT credentialSource survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "default-credential",
            parametersBlock = "credentialSource.set(AzureCredentialSource.DEFAULT)"
        )
    }

    test("BuildService with MANAGED_IDENTITY credentialSource survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "managed-identity",
            parametersBlock = """
                credentialSource.set(AzureCredentialSource.MANAGED_IDENTITY)
                clientId.set("00000000-0000-0000-0000-000000000000")
            """.trimIndent()
        )
    }

    test("BuildService with CLIENT_SECRET credentialSource survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "client-secret",
            parametersBlock = """
                credentialSource.set(AzureCredentialSource.CLIENT_SECRET)
                tenantId.set("00000000-0000-0000-0000-000000000001")
                clientId.set("00000000-0000-0000-0000-000000000002")
                clientSecretRef.set(CredentialReference.EnvironmentVariable("AZURE_CLIENT_SECRET"))
            """.trimIndent()
        )
    }

    test("BuildService with SAS_TOKEN credentialSource survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "sas-token",
            parametersBlock = """
                credentialSource.set(AzureCredentialSource.SAS_TOKEN)
                sasTokenRef.set(CredentialReference.EnvironmentVariable("AZURE_STORAGE_SAS_TOKEN"))
            """.trimIndent()
        )
    }

    test("BuildService with STORAGE_ACCOUNT_KEY credentialSource survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "storage-account-key",
            parametersBlock = """
                credentialSource.set(AzureCredentialSource.STORAGE_ACCOUNT_KEY)
                accountName.set("myaccount")
                accountKeyRef.set(CredentialReference.EnvironmentVariable("AZURE_STORAGE_ACCOUNT_KEY"))
            """.trimIndent()
        )
    }

    test("BuildService with endpoint and CLIENT_SECRET together survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "endpoint-and-secret",
            parametersBlock = """
                endpoint.set("https://example.blob.core.windows.net")
                credentialSource.set(AzureCredentialSource.CLIENT_SECRET)
                tenantId.set("00000000-0000-0000-0000-000000000001")
                clientId.set("00000000-0000-0000-0000-000000000002")
                clientSecretRef.set(CredentialReference.EnvironmentVariable("AZURE_CLIENT_SECRET"))
            """.trimIndent()
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

        import com.kelvsyc.gradle.azure.AzureCredentialSource
        import com.kelvsyc.gradle.azure.storage.blob.BlobServiceClientBuildService
        import com.kelvsyc.gradle.azure.storage.blob.fixtures.BlobBuildServiceProbeTask
        import com.kelvsyc.gradle.clients.CredentialReference

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
