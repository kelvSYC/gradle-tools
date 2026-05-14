package com.kelvsyc.gradle.aws.java.secretsmanager

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `SecretCacheBuildService.Params`.
 *
 * `SecretCacheBuildService` introduces a `baseService: Property<SecretsManagerClientBuildService>` parameter —
 * a BuildService reference nested inside another BuildService's parameters. Gradle's config-cache codec can
 * only serialize this reference when the value is the `Provider<SecretsManagerClientBuildService>` obtained
 * from `gradle.sharedServices.registerIfAbsent` (stored as the registration name). A raw `ObjectFactory`-managed
 * instance bypasses the registry and cannot be serialized.
 *
 * ### Variant A — registered base service (expected to succeed)
 *
 * Both services are registered; the `Provider<SecretsManagerClientBuildService>` from registration is set on
 * `Params.baseService`. This is the correct usage pattern.
 *
 * ### Variant B — unregistered ObjectFactory instance (expected to fail)
 *
 * `baseService` is set to an instance created via `objects.newInstance()` rather than registered via
 * `registerIfAbsent`. Gradle cannot locate this instance by service name when restoring the config-cache
 * entry, so the build fails. This variant documents the constraint so that future changes in Gradle's CC
 * codec surface immediately if the rule ever relaxes.
 */
class SecretCacheConfigurationCacheSpec : FunSpec({
    test("Variant A - SecretCacheBuildService with registered base service survives config-cache round-trip") {
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

    test("Variant B - SecretCacheBuildService with unregistered base service instance fails config-cache") {
        val projectDir = writeVariantBProject()
        val outcome = IntegrationTestSupport.runProbe(
            projectDir, "probe", "--configuration-cache", "--stacktrace"
        )
        // FINDING: baseService must be set via the Provider<SecretsManagerClientBuildService> from
        // registerIfAbsent, not via an ObjectFactory-managed instance. The CC codec serializes
        // BuildService references by registration name; an unregistered instance has no name.
        outcome.shouldBeInstanceOf<ProbeOutcome.Failed>()
    }
})

private fun writeVariantAProject(): File {
    val projectDir = IntegrationTestSupport.newProjectDir("secret-cache-variant-a")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.aws.java.secretsmanager.SecretCacheBuildService
        import com.kelvsyc.gradle.aws.java.secretsmanager.SecretsManagerClientBuildService
        import com.kelvsyc.gradle.aws.java.secretsmanager.fixtures.SecretCacheBuildServiceProbeTask

        val smService = gradle.sharedServices.registerIfAbsent(
            "secretsManager",
            SecretsManagerClientBuildService::class
        ) { }

        val cacheService = gradle.sharedServices.registerIfAbsent(
            "secretCache",
            SecretCacheBuildService::class
        ) {
            parameters {
                baseService.set(smService)
            }
        }

        tasks.register<SecretCacheBuildServiceProbeTask>("probe") {
            service.set(cacheService)
            usesService(cacheService)
        }
        """.trimIndent()
    )
    return projectDir
}

private fun writeVariantBProject(): File {
    val projectDir = IntegrationTestSupport.newProjectDir("secret-cache-variant-b")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.aws.java.secretsmanager.SecretCacheBuildService
        import com.kelvsyc.gradle.aws.java.secretsmanager.SecretsManagerClientBuildService
        import com.kelvsyc.gradle.aws.java.secretsmanager.fixtures.SecretCacheBuildServiceProbeTask

        val rawSmService = objects.newInstance(SecretsManagerClientBuildService::class.java)

        val cacheService = gradle.sharedServices.registerIfAbsent(
            "secretCache",
            SecretCacheBuildService::class
        ) {
            parameters {
                baseService.set(rawSmService)
            }
        }

        tasks.register<SecretCacheBuildServiceProbeTask>("probe") {
            service.set(cacheService)
            usesService(cacheService)
        }
        """.trimIndent()
    )
    return projectDir
}
