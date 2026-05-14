package com.kelvsyc.gradle.aws.java.s3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `S3TransferManagerBuildService.Params`.
 *
 * `S3TransferManagerBuildService` introduces a `baseService: Property<S3AsyncClientBuildService>` parameter —
 * a BuildService reference nested inside another BuildService's parameters. Gradle's config-cache codec can
 * only serialize this reference when the value is the `Provider<S3AsyncClientBuildService>` obtained from
 * `gradle.sharedServices.registerIfAbsent` (stored as the registration name). A raw `ObjectFactory`-managed
 * instance bypasses the registry and cannot be serialized.
 *
 * ### Variant A — registered base service (expected to succeed)
 *
 * Both services are registered; the `Provider<S3AsyncClientBuildService>` from registration is set on
 * `Params.baseService`. This is the correct usage pattern.
 *
 * ### Variant B — unregistered ObjectFactory instance (expected to fail)
 *
 * `baseService` is set to an instance created via `objects.newInstance()` rather than registered via
 * `registerIfAbsent`. Gradle cannot locate this instance by service name when restoring the config-cache
 * entry, so the build fails. This variant documents the constraint so that future changes in Gradle's CC
 * codec surface immediately if the rule ever relaxes.
 */
class S3TransferManagerConfigurationCacheSpec : FunSpec({
    test("Variant A - S3TransferManagerBuildService with registered base service survives config-cache round-trip") {
        val projectDir = writeVariantAProject()

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

    test("Variant B - S3TransferManagerBuildService with unregistered base service instance fails config-cache") {
        val projectDir = writeVariantBProject()
        val outcome = IntegrationTestSupport.runProbe(
            projectDir, "probe", "--configuration-cache", "--stacktrace"
        )
        // FINDING: baseService must be set via the Provider<S3AsyncClientBuildService> from
        // registerIfAbsent, not via an ObjectFactory-managed instance. The CC codec serializes
        // BuildService references by registration name; an unregistered instance has no name.
        outcome.shouldBeInstanceOf<ProbeOutcome.Failed>()
    }
})

private fun writeVariantAProject(): File {
    val projectDir = IntegrationTestSupport.newProjectDir("s3-transfer-manager-variant-a")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.aws.java.s3.S3AsyncClientBuildService
        import com.kelvsyc.gradle.aws.java.s3.S3TransferManagerBuildService
        import com.kelvsyc.gradle.aws.java.s3.fixtures.S3TransferManagerBuildServiceProbeTask

        val s3AsyncService = gradle.sharedServices.registerIfAbsent(
            "s3-async",
            S3AsyncClientBuildService::class
        ) { }

        val transferManagerService = gradle.sharedServices.registerIfAbsent(
            "s3-transfer-manager",
            S3TransferManagerBuildService::class
        ) {
            parameters {
                baseService.set(s3AsyncService)
            }
        }

        tasks.register<S3TransferManagerBuildServiceProbeTask>("probe") {
            service.set(transferManagerService)
            usesService(transferManagerService)
        }
        """.trimIndent()
    )
    return projectDir
}

private fun writeVariantBProject(): File {
    val projectDir = IntegrationTestSupport.newProjectDir("s3-transfer-manager-variant-b")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.aws.java.s3.S3AsyncClientBuildService
        import com.kelvsyc.gradle.aws.java.s3.S3TransferManagerBuildService
        import com.kelvsyc.gradle.aws.java.s3.fixtures.S3TransferManagerBuildServiceProbeTask

        val rawAsyncService = objects.newInstance(S3AsyncClientBuildService::class.java)

        val transferManagerService = gradle.sharedServices.registerIfAbsent(
            "s3-transfer-manager",
            S3TransferManagerBuildService::class
        ) {
            parameters {
                baseService.set(rawAsyncService)
            }
        }

        tasks.register<S3TransferManagerBuildServiceProbeTask>("probe") {
            service.set(transferManagerService)
            usesService(transferManagerService)
        }
        """.trimIndent()
    )
    return projectDir
}
