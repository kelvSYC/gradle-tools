package com.kelvsyc.gradle.azure.containerregistry

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `ContainerRegistryClientBuildService.Params` and
 * `ContainerRepositoryClientBuildService.Params`.
 *
 * The registry service exposes `endpoint: Property<String>` plus the serializable credential properties of
 * `AzureBuildServiceParams`; the SDK `TokenCredential` is built inside `createClient()`, never stored. The
 * repository service chains to the registry service through a `@ServiceReference` and holds a repository name. Each
 * test verifies that a first invocation stores the configuration cache entry and a second invocation reuses it.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("ContainerRegistryClientBuildService with no parameters survives config-cache round-trip") {
        assertRegistryRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("ContainerRegistryClientBuildService with endpoint survives config-cache round-trip") {
        assertRegistryRoundTripCleanly(
            name = "endpoint",
            parametersBlock = """endpoint.set("https://example.azurecr.io")"""
        )
    }

    test("ContainerRegistryClientBuildService with DEFAULT credentialSource survives config-cache round-trip") {
        assertRegistryRoundTripCleanly(
            name = "default-credential",
            parametersBlock = """
                endpoint.set("https://example.azurecr.io")
                credentialSource.set(AzureCredentialSource.DEFAULT)
            """.trimIndent()
        )
    }

    test("ContainerRegistryClientBuildService with CLIENT_SECRET credentialSource survives config-cache round-trip") {
        assertRegistryRoundTripCleanly(
            name = "client-secret",
            parametersBlock = """
                endpoint.set("https://example.azurecr.io")
                credentialSource.set(AzureCredentialSource.CLIENT_SECRET)
                tenantId.set("00000000-0000-0000-0000-000000000001")
                clientId.set("00000000-0000-0000-0000-000000000002")
                clientSecretRef.set(CredentialReference.EnvironmentVariable("AZURE_CLIENT_SECRET"))
            """.trimIndent()
        )
    }

    test("ContainerRepositoryClientBuildService chained to a registry service survives config-cache round-trip") {
        val projectDir = writeProbeProject(
            name = "repository",
            body = """
                val registry = gradle.sharedServices.registerIfAbsent(
                    "registry",
                    ContainerRegistryClientBuildService::class
                ) {
                    parameters {
                        endpoint.set("https://example.azurecr.io")
                    }
                }
                val repositoryService = gradle.sharedServices.registerIfAbsent(
                    "repository",
                    ContainerRepositoryClientBuildService::class
                ) {
                    parameters {
                        registryService.set(registry)
                        repositoryName.set("myorg/myrepo")
                    }
                }

                tasks.register<RepositoryBuildServiceProbeTask>("probe") {
                    service.set(repositoryService)
                    usesService(repositoryService)
                }
            """.trimIndent()
        )
        assertRoundTripCleanly(projectDir)
    }
})

private fun assertRegistryRoundTripCleanly(name: String, parametersBlock: String) {
    val projectDir = writeProbeProject(
        name = name,
        body = """
            val registry = gradle.sharedServices.registerIfAbsent(
                "registry",
                ContainerRegistryClientBuildService::class
            ) {
                parameters {
                    $parametersBlock
                }
            }

            tasks.register<RegistryBuildServiceProbeTask>("probe") {
                service.set(registry)
                usesService(registry)
            }
        """.trimIndent()
    )
    assertRoundTripCleanly(projectDir)
}

private fun assertRoundTripCleanly(projectDir: File) {
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

private fun writeProbeProject(name: String, body: String): File {
    val projectDir = IntegrationTestSupport.newProjectDir("acr-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.azure.AzureCredentialSource
        import com.kelvsyc.gradle.azure.containerregistry.ContainerRegistryClientBuildService
        import com.kelvsyc.gradle.azure.containerregistry.ContainerRepositoryClientBuildService
        import com.kelvsyc.gradle.azure.containerregistry.fixtures.RegistryBuildServiceProbeTask
        import com.kelvsyc.gradle.azure.containerregistry.fixtures.RepositoryBuildServiceProbeTask
        import com.kelvsyc.gradle.clients.CredentialReference

        $body
        """.trimIndent()
    )
    return projectDir
}
