package com.kelvsyc.gradle.aws.java.secretsmanager

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `SecretsManagerClientBuildService.Params` (a.k.a. `AwsBuildServiceParams`).
 *
 * A trimmed matrix (empty params + static credentials) is sufficient to catch any per-component override
 * that bypasses `AbstractAwsJavaClientBuildService`. For the full `AwsCredentialSource` branch matrix, see
 * `aws-sns-java-base`'s `BuildServiceConfigurationCacheSpec`. For the nested `baseService` reference case,
 * see [SecretCacheConfigurationCacheSpec].
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("SecretsManagerClientBuildService with no parameter values survives config-cache round-trip") {
        assertParamsRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("SecretsManagerClientBuildService with static credentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "static-credentials",
            parametersBlock = """
                regionId.set("us-east-1")
                credentialSource.set(AwsCredentialSource.STATIC)
                accessKeyIdRef.set(CredentialReference.Literal("ak"))
                secretAccessKeyRef.set(CredentialReference.Literal("sk"))
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
    val projectDir = IntegrationTestSupport.newProjectDir("secrets-manager-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.aws.java.AwsCredentialSource
        import com.kelvsyc.gradle.clients.CredentialReference
        import com.kelvsyc.gradle.aws.java.secretsmanager.SecretsManagerClientBuildService
        import com.kelvsyc.gradle.aws.java.secretsmanager.fixtures.SecretsManagerClientBuildServiceProbeTask

        val smService = gradle.sharedServices.registerIfAbsent(
            "secretsManager",
            SecretsManagerClientBuildService::class
        ) {
            parameters {
                $parametersBlock
            }
        }

        tasks.register<SecretsManagerClientBuildServiceProbeTask>("probe") {
            service.set(smService)
            usesService(smService)
        }
        """.trimIndent()
    )
    return projectDir
}
