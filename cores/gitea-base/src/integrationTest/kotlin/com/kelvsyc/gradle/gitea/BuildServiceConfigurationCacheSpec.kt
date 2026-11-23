package com.kelvsyc.gradle.gitea

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `GiteaBasicClientBuildService.Params` and
 * `GiteaBearerClientBuildService.Params`.
 *
 * `baseUrl` and `username` are `Property<String>`; `passwordRef` and `tokenRef` are
 * `Property<CredentialReference>`, which stores only the lookup key (an env-var name or system-property name), never
 * the credential value. Each test verifies that a first invocation stores the configuration cache entry and a second
 * invocation reuses it.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("GiteaBasicClientBuildService with no parameters survives config-cache round-trip") {
        assertParamsRoundTripCleanly(ServiceKind.BASIC, name = "basic-no-params", parametersBlock = "")
    }

    test("GiteaBasicClientBuildService with url and credentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            ServiceKind.BASIC,
            name = "basic-credentials",
            parametersBlock = """
                baseUrl.set("https://gitea.example.com/")
                username.set("user")
                passwordRef.set(CredentialReference.EnvironmentVariable("GITEA_PASSWORD"))
            """.trimIndent()
        )
    }

    test("GiteaBearerClientBuildService with no parameters survives config-cache round-trip") {
        assertParamsRoundTripCleanly(ServiceKind.BEARER, name = "bearer-no-params", parametersBlock = "")
    }

    test("GiteaBearerClientBuildService with url and token survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            ServiceKind.BEARER,
            name = "bearer-token",
            parametersBlock = """
                baseUrl.set("https://gitea.example.com/")
                tokenRef.set(CredentialReference.EnvironmentVariable("GITEA_TOKEN"))
            """.trimIndent()
        )
    }
})

private enum class ServiceKind(val serviceClass: String, val probeClass: String) {
    BASIC("GiteaBasicClientBuildService", "GiteaBasicBuildServiceProbeTask"),
    BEARER("GiteaBearerClientBuildService", "GiteaBearerBuildServiceProbeTask"),
}

private fun assertParamsRoundTripCleanly(kind: ServiceKind, name: String, parametersBlock: String) {
    val projectDir = writeConfigCacheProbeProject(kind, name, parametersBlock)

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

private fun writeConfigCacheProbeProject(kind: ServiceKind, name: String, parametersBlock: String): File {
    val projectDir = IntegrationTestSupport.newProjectDir("gitea-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.clients.CredentialReference
        import com.kelvsyc.gradle.gitea.${kind.serviceClass}
        import com.kelvsyc.gradle.gitea.fixtures.${kind.probeClass}

        val giteaService = gradle.sharedServices.registerIfAbsent(
            "gitea",
            ${kind.serviceClass}::class
        ) {
            parameters {
                $parametersBlock
            }
        }

        tasks.register<${kind.probeClass}>("probe") {
            service.set(giteaService)
            usesService(giteaService)
        }
        """.trimIndent()
    )
    return projectDir
}
