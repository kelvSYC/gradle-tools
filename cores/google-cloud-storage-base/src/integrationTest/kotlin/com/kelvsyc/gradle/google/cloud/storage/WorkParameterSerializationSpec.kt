package com.kelvsyc.gradle.google.cloud.storage

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe #2: serialization of `WorkParameters` at `WorkerExecutor.submit()` time for the GCS module.
 *
 * Variant A baseline mirrors the production shape (`Property<StorageClientBuildService>` on WorkParams).
 * Variant B records whether the BYO retrofit (`Property<Storage>` on WorkParams) is viable.
 *
 * No `BuildServiceParameters` are set on the registered service; see `BuildServiceConfigurationCacheSpec`
 * for the isolation findings that motivate that choice.
 */
class WorkParameterSerializationSpec : FunSpec({
    test("Variant A baseline - Property<StorageClientBuildService> on WorkParameters succeeds") {
        val projectDir = writeVariantAProject()
        val outcome = IntegrationTestSupport.runProbe(
            projectDir, "probe", "--configuration-cache", "--stacktrace"
        )
        val succeeded = outcome.shouldBeInstanceOf<ProbeOutcome.Succeeded>()
        succeeded.result.task(":probe")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    test("Variant B BYO - Property<Storage> on WorkParameters fails with non-serializable error") {
        val projectDir = writeVariantBProject()
        val outcome = IntegrationTestSupport.runProbe(
            projectDir, "probe", "--configuration-cache", "--stacktrace"
        )
        // FINDING: the BYO retrofit is structurally infeasible for GCS action-dispatch; the live `Storage`
        // implementation is not `Serializable`. Same conclusion as AWS SNS — `Property<*BuildService>`
        // remains the correct end-state for action-dispatch components.
        val failed = outcome.shouldBeInstanceOf<ProbeOutcome.Failed>()
        failed.message shouldContain "Could not serialize value of type"
    }
})

private fun writeVariantAProject(): File {
    val projectDir = IntegrationTestSupport.newProjectDir("gcs-worker-variant-a")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.google.cloud.storage.StorageClientBuildService
        import com.kelvsyc.gradle.google.cloud.storage.fixtures.BuildServiceWorkerProbeTask

        val storageService = gradle.sharedServices.registerIfAbsent(
            "storage",
            StorageClientBuildService::class
        ) { }

        tasks.register<BuildServiceWorkerProbeTask>("probe") {
            service.set(storageService)
            usesService(storageService)
        }
        """.trimIndent()
    )
    return projectDir
}

private fun writeVariantBProject(): File {
    val projectDir = IntegrationTestSupport.newProjectDir("gcs-worker-variant-b")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.google.cloud.storage.fixtures.ByoClientWorkerProbeTask

        tasks.register<ByoClientWorkerProbeTask>("probe") {
            projectId.set("test-project")
        }
        """.trimIndent()
    )
    return projectDir
}
