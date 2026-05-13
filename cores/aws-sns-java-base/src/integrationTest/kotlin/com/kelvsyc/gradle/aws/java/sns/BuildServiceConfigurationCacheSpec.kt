package com.kelvsyc.gradle.aws.java.sns

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe #1: configuration-cache round-trip of `SnsClientBuildService.Params`.
 *
 * Each test encodes the **observed** current behavior. A failing test means the underlying Gradle/SDK
 * behavior has shifted in either direction — investigate before "fixing" the test.
 *
 * ### Findings (as of the integration-test introduction)
 *
 * - With no `BuildServiceParameters` properties set, the BuildService isolation round-trip succeeds and the
 *   stored configuration cache entry is reused on a second invocation.
 * - Setting `Property<Region>` to `Region.US_EAST_1` makes parameter isolation fail with
 *   `Could not serialize value of type Region` — the AWS SDK's `Region` value type is not handled by
 *   Gradle's config-cache serializer.
 * - Setting `Property<AwsCredentialsProvider>` to a `StaticCredentialsProvider` fails with
 *   `Could not serialize value of type StaticCredentialsProvider`.
 *
 * Together these say: **the current production shape (`Property<Region>` + `Property<AwsCredentialsProvider>`
 * on `BuildServiceParameters`) is not configuration-cache compatible.** Resolving it requires refactoring
 * the BuildService to hold serializable raw values (region name string, access-key/secret-key strings) and
 * constructing the SDK types lazily inside `createClient()`.
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

    test("BuildService with Region property currently fails config-cache isolation") {
        val projectDir = writeConfigCacheProbeProject(
            parametersBlock = "region.set(Region.US_EAST_1)"
        )
        val outcome = IntegrationTestSupport.runProbe(
            projectDir, "probe", "--configuration-cache", "--stacktrace"
        )
        val failed = outcome.shouldBeInstanceOf<ProbeOutcome.Failed>()
        failed.message shouldContain "Could not serialize value of type Region"
    }

    test("BuildService with StaticCredentialsProvider currently fails config-cache isolation") {
        val projectDir = writeConfigCacheProbeProject(
            parametersBlock =
                "credentials.set(StaticCredentialsProvider.create(AwsBasicCredentials.create(\"ak\", \"sk\")))"
        )
        val outcome = IntegrationTestSupport.runProbe(
            projectDir, "probe", "--configuration-cache", "--stacktrace"
        )
        val failed = outcome.shouldBeInstanceOf<ProbeOutcome.Failed>()
        failed.message shouldContain "Could not serialize value of type StaticCredentialsProvider"
    }
})

private fun writeConfigCacheProbeProject(parametersBlock: String): File {
    val projectDir = IntegrationTestSupport.newProjectDir("sns-config-cache-probe")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.aws.java.sns.SnsClientBuildService
        import com.kelvsyc.gradle.aws.java.sns.fixtures.SnsBuildServiceProbeTask
        import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
        import software.amazon.awssdk.auth.credentials.StaticCredentialsProvider
        import software.amazon.awssdk.regions.Region

        val snsService = gradle.sharedServices.registerIfAbsent(
            "sns",
            SnsClientBuildService::class
        ) {
            parameters {
                $parametersBlock
            }
        }

        tasks.register<SnsBuildServiceProbeTask>("probe") {
            service.set(snsService)
            usesService(snsService)
        }
        """.trimIndent()
    )
    return projectDir
}
