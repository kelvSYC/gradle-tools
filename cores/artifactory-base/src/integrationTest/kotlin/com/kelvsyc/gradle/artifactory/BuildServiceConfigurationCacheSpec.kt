package com.kelvsyc.gradle.artifactory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `ArtifactoryClientBuildService.Params`.
 *
 * `Params.credentials` is `Property<PasswordCredentials>`. Unlike the AWS and GCP params that were
 * redesigned to hold only primitives, this property holds a Gradle interface type. Two questions are probed:
 *
 * 1. Does an `ObjectFactory`-managed `PasswordCredentials` instance round-trip cleanly? (Gradle managed
 *    types participate in the CC codec via the managed-type serialization path.)
 * 2. Does a plain non-managed implementation of `PasswordCredentials` (i.e. [FakePasswordCredentials])
 *    survive the CC round-trip? (Expected: no — it is neither a Gradle managed type nor `Serializable`.)
 *
 * A failing "managed credentials" test would justify decomposing `Property<PasswordCredentials>` into
 * `Property<String>` username + password fields (see Phase C decomposition PR). A failing "fake credentials"
 * test confirms the decomposition is necessary for defensive correctness even if the managed path works.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("ArtifactoryClientBuildService with no parameters survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "no-params",
            credentialsBlock = ""
        )
    }

    test("ArtifactoryClientBuildService with url-only survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "url-only",
            credentialsBlock = """url.set("https://example.jfrog.io/artifactory")"""
        )
    }

    test("ArtifactoryClientBuildService with ObjectFactory-managed PasswordCredentials survives config-cache round-trip") {
        assertParamsRoundTripCleanly(
            name = "managed-credentials",
            credentialsBlock = """
                url.set("https://example.jfrog.io/artifactory")
                credentials.set(project.objects.newInstance(org.gradle.api.credentials.PasswordCredentials::class.java).also {
                    it.username = "user"
                    it.password = "s3cr3t"
                })
            """.trimIndent()
        )
    }

    test("ArtifactoryClientBuildService with non-managed PasswordCredentials fails config-cache") {
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
    val projectDir = IntegrationTestSupport.newProjectDir("artifactory-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.artifactory.ArtifactoryClientBuildService
        import com.kelvsyc.gradle.artifactory.fixtures.ArtifactoryClientBuildServiceProbeTask

        val artService = gradle.sharedServices.registerIfAbsent(
            "artifactory",
            ArtifactoryClientBuildService::class
        ) {
            parameters {
                $credentialsBlock
            }
        }

        tasks.register<ArtifactoryClientBuildServiceProbeTask>("probe") {
            service.set(artService)
            usesService(artService)
        }
        """.trimIndent()
    )
    return projectDir
}

private fun writeFakeCredentialsProject(): File {
    val projectDir = IntegrationTestSupport.newProjectDir("artifactory-config-cache-fake-credentials")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.artifactory.ArtifactoryClientBuildService
        import com.kelvsyc.gradle.artifactory.fixtures.ArtifactoryClientBuildServiceProbeTask
        import com.kelvsyc.gradle.artifactory.fixtures.FakePasswordCredentials

        val artService = gradle.sharedServices.registerIfAbsent(
            "artifactory",
            ArtifactoryClientBuildService::class
        ) {
            parameters {
                url.set("https://example.jfrog.io/artifactory")
                credentials.set(FakePasswordCredentials("user", "s3cr3t"))
            }
        }

        tasks.register<ArtifactoryClientBuildServiceProbeTask>("probe") {
            service.set(artService)
            usesService(artService)
        }
        """.trimIndent()
    )
    return projectDir
}
