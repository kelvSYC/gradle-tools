package com.kelvsyc.gradle.bitbucket.cloud

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `BitbucketCloudClientBuildService.Params`.
 *
 * `Params.credentials` is `Property<PasswordCredentials>`. Unlike the AWS and GCP params that hold only
 * primitives, this property holds a Gradle interface type. Two questions are probed:
 *
 * 1. Does an `ObjectFactory`-managed `PasswordCredentials` instance round-trip cleanly? (Gradle managed
 *    types participate in the CC codec via the managed-type serialization path.)
 * 2. Does a plain non-managed implementation of `PasswordCredentials` (i.e. [FakePasswordCredentials])
 *    survive the CC round-trip? (Expected: no — it is neither a Gradle managed type nor `Serializable`.)
 *
 * A failing "managed credentials" test would justify decomposing `Property<PasswordCredentials>` into
 * `Property<String>` username + password fields. A failing "fake credentials" test confirms the decomposition
 * is necessary for defensive correctness even if the managed path works.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("BitbucketCloudClientBuildService with no parameters survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "no-params",
            credentialsBlock = ""
        )
    }

    test("BitbucketCloudClientBuildService with baseUrl-only survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "base-url-only",
            credentialsBlock = """baseUrl.set("https://api.bitbucket.org/2.0/")"""
        )
    }

    test("BitbucketCloudClientBuildService with ObjectFactory-managed PasswordCredentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "managed-credentials",
            credentialsBlock = """
                credentials.set(project.objects.newInstance(org.gradle.api.credentials.PasswordCredentials::class.java).also {
                    it.username = "user"
                    it.password = "apppassword"
                })
            """.trimIndent()
        )
    }

    test("BitbucketCloudClientBuildService with non-managed PasswordCredentials fails config-cache") {
        val projectDir = writeFakeCredentialsProject()
        val outcome = IntegrationTestSupport.runProbe(
            projectDir, "probe", "--configuration-cache", "--stacktrace"
        )
        // FINDING: a plain PasswordCredentials implementation that is not a Gradle managed type and
        // does not implement Serializable cannot be serialized by Gradle's CC codec. Users must either
        // use objects.newInstance(PasswordCredentials::class.java) or wait for the decomposition to
        // Property<String> username + password (Phase C follow-up).
        outcome.shouldBeInstanceOf<ProbeOutcome.Failed>()
    }
})

private fun assertParamsRoundTripCleanly(name: String, credentialsBlock: String) {
    val projectDir = writeConfigCacheProbeProject(name = name, credentialsBlock = credentialsBlock)

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

private fun writeConfigCacheProbeProject(name: String, credentialsBlock: String): File {
    val projectDir = IntegrationTestSupport.newProjectDir("bitbucket-cloud-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.bitbucket.cloud.BitbucketCloudClientBuildService
        import com.kelvsyc.gradle.bitbucket.cloud.fixtures.BitbucketCloudClientBuildServiceProbeTask

        val bbService = gradle.sharedServices.registerIfAbsent(
            "bitbucketCloud",
            BitbucketCloudClientBuildService::class
        ) {
            parameters {
                $credentialsBlock
            }
        }

        tasks.register<BitbucketCloudClientBuildServiceProbeTask>("probe") {
            service.set(bbService)
            usesService(bbService)
        }
        """.trimIndent()
    )
    return projectDir
}

private fun writeFakeCredentialsProject(): File {
    val projectDir = IntegrationTestSupport.newProjectDir("bitbucket-cloud-config-cache-fake-credentials")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.bitbucket.cloud.BitbucketCloudClientBuildService
        import com.kelvsyc.gradle.bitbucket.cloud.fixtures.BitbucketCloudClientBuildServiceProbeTask
        import com.kelvsyc.gradle.bitbucket.cloud.fixtures.FakePasswordCredentials

        val bbService = gradle.sharedServices.registerIfAbsent(
            "bitbucketCloud",
            BitbucketCloudClientBuildService::class
        ) {
            parameters {
                credentials.set(FakePasswordCredentials("user", "apppassword"))
            }
        }

        tasks.register<BitbucketCloudClientBuildServiceProbeTask>("probe") {
            service.set(bbService)
            usesService(bbService)
        }
        """.trimIndent()
    )
    return projectDir
}
