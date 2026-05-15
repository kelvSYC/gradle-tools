package com.kelvsyc.gradle.aws.java.sns

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe #1: configuration-cache round-trip of `SnsClientBuildService.Params` (a.k.a. `AwsBuildServiceParams`).
 *
 * Each test pins down the **observed** behavior of the parameter shape — `regionId: Property<String>`,
 * `credentialSource: Property<AwsCredentialSource>`, `accessKeyIdRef/secretAccessKeyRef/sessionTokenRef: Property<CredentialReference>`,
 * `credentialsProfile: Property<String>`. A failing test means either Gradle's config-cache serializer or the
 * AWS Java extensions have regressed; investigate before "fixing" the test.
 *
 * ### What this characterizes
 *
 * The AWS Java SDK's `Region` and `AwsCredentialsProvider` types are not serializable by Gradle's
 * configuration-cache codec, so `AwsBuildServiceParams` exposes only serializable primitives and the SDK
 * types are reconstructed inside `createClient()`. These tests exercise that contract: each branch of
 * [com.kelvsyc.gradle.aws.java.AwsCredentialSource] survives the BuildService parameter-isolation round-trip,
 * region names round-trip, and a second invocation reuses the stored configuration cache entry rather than
 * rebuilding it.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("BuildService with no parameter values survives config-cache round-trip") {
        assertParamsRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("BuildService with regionId set survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "regionId",
            parametersBlock = """regionId.set("us-east-1")"""
        )
    }

    test("BuildService with anonymous credentialSource survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "anonymous",
            parametersBlock = "credentialSource.set(AwsCredentialSource.ANONYMOUS)"
        )
    }

    test("BuildService with static credentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "static-credentials",
            parametersBlock = """
                credentialSource.set(AwsCredentialSource.STATIC)
                accessKeyIdRef.set(CredentialReference.Literal("ak"))
                secretAccessKeyRef.set(CredentialReference.Literal("sk"))
            """.trimIndent()
        )
    }

    test("BuildService with session credentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "session-credentials",
            parametersBlock = """
                credentialSource.set(AwsCredentialSource.STATIC)
                accessKeyIdRef.set(CredentialReference.Literal("ak"))
                secretAccessKeyRef.set(CredentialReference.Literal("sk"))
                sessionTokenRef.set(CredentialReference.Literal("tok"))
            """.trimIndent()
        )
    }

    test("BuildService with profile credentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "profile-credentials",
            parametersBlock = """
                credentialSource.set(AwsCredentialSource.PROFILE)
                credentialsProfile.set("default")
            """.trimIndent()
        )
    }

    test("BuildService with default credentials chain survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "default-chain",
            parametersBlock = "credentialSource.set(AwsCredentialSource.DEFAULT_CHAIN)"
        )
    }

    test("BuildService with regionId and static credentials together survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "region-and-static",
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
    val projectDir = IntegrationTestSupport.newProjectDir("sns-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.aws.java.AwsCredentialSource
        import com.kelvsyc.gradle.clients.CredentialReference
        import com.kelvsyc.gradle.aws.java.sns.SnsClientBuildService
        import com.kelvsyc.gradle.aws.java.sns.fixtures.SnsBuildServiceProbeTask

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
