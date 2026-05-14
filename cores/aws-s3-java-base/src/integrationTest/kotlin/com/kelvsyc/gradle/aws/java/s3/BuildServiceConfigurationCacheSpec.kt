package com.kelvsyc.gradle.aws.java.s3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `S3ClientBuildService.Params` (a.k.a. `AwsBuildServiceParams`).
 *
 * `S3ClientBuildService` adds no extra parameters beyond `AwsBuildServiceParams`, making it the purest
 * possible regression sentinel for the abstract base. A trimmed test matrix (empty params + static
 * credentials) is sufficient to catch any per-component override that bypasses `AbstractAwsJavaClientBuildService`.
 *
 * For the full `AwsCredentialSource` branch matrix, see `aws-sns-java-base`'s `BuildServiceConfigurationCacheSpec`.
 * For the nested `baseService` BuildService-reference case, see [S3TransferManagerConfigurationCacheSpec].
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("S3ClientBuildService with no parameter values survives config-cache round-trip") {
        assertParamsRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("S3ClientBuildService with static credentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "static-credentials",
            parametersBlock = """
                regionId.set("us-east-1")
                credentialSource.set(AwsCredentialSource.STATIC)
                accessKeyId.set("ak")
                secretAccessKey.set("sk")
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
    val projectDir = IntegrationTestSupport.newProjectDir("s3-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.aws.java.AwsCredentialSource
        import com.kelvsyc.gradle.aws.java.s3.S3ClientBuildService
        import com.kelvsyc.gradle.aws.java.s3.fixtures.S3ClientBuildServiceProbeTask

        val s3Service = gradle.sharedServices.registerIfAbsent(
            "s3",
            S3ClientBuildService::class
        ) {
            parameters {
                $parametersBlock
            }
        }

        tasks.register<S3ClientBuildServiceProbeTask>("probe") {
            service.set(s3Service)
            usesService(s3Service)
        }
        """.trimIndent()
    )
    return projectDir
}
