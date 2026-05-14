package com.kelvsyc.gradle.aws.java.imds

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `ImdsClientBuildService.Params` and
 * `ImdsAsyncClientBuildService.Params`.
 *
 * Both services share the same parameter shape:
 * - `endpoint: Property<String>` — trivially CC-safe
 * - `endpointMode: Property<EndpointMode>` — `EndpointMode` is a standard Java enum from the AWS SDK,
 *   so Gradle's CC codec handles it via the built-in enum serialization path (by name). Expected to succeed.
 *
 * These tests confirm the expectation and serve as regression sentinels: if a future AWS SDK release changes
 * `EndpointMode` away from a standard enum, one of these tests will go red.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    // --- ImdsClientBuildService (sync) ---

    test("ImdsClientBuildService with no parameters survives config-cache round-trip") {
        assertImdsRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("ImdsClientBuildService with endpoint-only survives config-cache round-trip") {
        assertImdsRoundTripCleanly(
            name = "endpoint-only",
            parametersBlock = """endpoint.set("http://169.254.169.254/")"""
        )
    }

    test("ImdsClientBuildService with endpointMode enum survives config-cache round-trip") {
        assertImdsRoundTripCleanly(
            name = "endpoint-mode",
            parametersBlock = "endpointMode.set(EndpointMode.IPV4)"
        )
    }

    test("ImdsClientBuildService with endpoint and endpointMode together survives config-cache round-trip") {
        assertImdsRoundTripCleanly(
            name = "endpoint-and-mode",
            parametersBlock = """
                endpoint.set("http://169.254.169.254/")
                endpointMode.set(EndpointMode.IPV6)
            """.trimIndent()
        )
    }

    // --- ImdsAsyncClientBuildService ---

    test("ImdsAsyncClientBuildService with no parameters survives config-cache round-trip") {
        assertImdsAsyncRoundTripCleanly(name = "no-params", parametersBlock = "")
    }

    test("ImdsAsyncClientBuildService with endpointMode enum survives config-cache round-trip") {
        assertImdsAsyncRoundTripCleanly(
            name = "endpoint-mode",
            parametersBlock = "endpointMode.set(EndpointMode.IPV4)"
        )
    }
})

private fun assertImdsRoundTripCleanly(name: String, parametersBlock: String) {
    val projectDir = writeImdsProbeProject(name = name, parametersBlock = parametersBlock)

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

private fun assertImdsAsyncRoundTripCleanly(name: String, parametersBlock: String) {
    val projectDir = writeImdsAsyncProbeProject(name = name, parametersBlock = parametersBlock)

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

private fun writeImdsProbeProject(name: String, parametersBlock: String): File {
    val projectDir = IntegrationTestSupport.newProjectDir("imds-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import software.amazon.awssdk.imds.EndpointMode
        import com.kelvsyc.gradle.aws.java.imds.ImdsClientBuildService
        import com.kelvsyc.gradle.aws.java.imds.fixtures.ImdsClientBuildServiceProbeTask

        val imdsService = gradle.sharedServices.registerIfAbsent(
            "imds",
            ImdsClientBuildService::class
        ) {
            parameters {
                $parametersBlock
            }
        }

        tasks.register<ImdsClientBuildServiceProbeTask>("probe") {
            service.set(imdsService)
            usesService(imdsService)
        }
        """.trimIndent()
    )
    return projectDir
}

private fun writeImdsAsyncProbeProject(name: String, parametersBlock: String): File {
    val projectDir = IntegrationTestSupport.newProjectDir("imds-async-config-cache-$name")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import software.amazon.awssdk.imds.EndpointMode
        import com.kelvsyc.gradle.aws.java.imds.ImdsAsyncClientBuildService
        import com.kelvsyc.gradle.aws.java.imds.fixtures.ImdsAsyncClientBuildServiceProbeTask

        val imdsService = gradle.sharedServices.registerIfAbsent(
            "imds-async",
            ImdsAsyncClientBuildService::class
        ) {
            parameters {
                $parametersBlock
            }
        }

        tasks.register<ImdsAsyncClientBuildServiceProbeTask>("probe") {
            service.set(imdsService)
            usesService(imdsService)
        }
        """.trimIndent()
    )
    return projectDir
}
