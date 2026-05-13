package com.kelvsyc.gradle.aws.java.sns

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe #2: serialization of `WorkParameters` at `WorkerExecutor.submit()` time.
 *
 * Variant A (baseline) dispatches a `WorkAction` whose `WorkParameters` exposes the SNS BuildService via
 * `Property<SnsClientBuildService>` — the production shape used by `PublishAction`.
 *
 * Variant B (proposed BYO retrofit) dispatches a `WorkAction` whose `WorkParameters` exposes the live SDK
 * client via `Property<SnsClient>` — the asymmetric shape mirroring the task-level BYO pattern. The probe
 * records the observed outcome so the deferred BYO question stays answered.
 *
 * Note: no `BuildServiceParameters` properties are set on the SNS service here. The config-cache probe
 * already documents that explicit `region` or `credentials` values currently break BuildService
 * parameter isolation; this spec is about the WorkerExecutor boundary, so we use the params-unset path to
 * isolate that boundary from the BuildService isolation failure mode.
 */
class WorkParameterSerializationSpec : FunSpec({
    test("Variant A baseline - Property<SnsClientBuildService> on WorkParameters succeeds") {
        val projectDir = writeVariantAProject()
        val outcome = IntegrationTestSupport.runProbe(
            projectDir, "probe", "--configuration-cache", "--stacktrace"
        )
        val succeeded = outcome.shouldBeInstanceOf<ProbeOutcome.Succeeded>()
        succeeded.result.task(":probe")?.outcome shouldBe TaskOutcome.SUCCESS
    }

    test("Variant B BYO - Property<SnsClient> on WorkParameters fails with NotSerializableException") {
        val projectDir = writeVariantBProject()
        val outcome = IntegrationTestSupport.runProbe(
            projectDir, "probe", "--configuration-cache", "--stacktrace"
        )
        // FINDING: the BYO retrofit is structurally infeasible for action-dispatch components. The live
        // SDK client is serialized at WorkerExecutor.submit() time and the SDK's `DefaultSnsClient` is not
        // `Serializable`. This confirms that `Property<*BuildService>` on `WorkParameters` is the correct
        // end-state for action-dispatch — not a stop-gap.
        val failed = outcome.shouldBeInstanceOf<ProbeOutcome.Failed>()
        failed.message shouldContain "Could not serialize value of type DefaultSnsClient"
    }
})

private fun writeVariantAProject(): File {
    val projectDir = IntegrationTestSupport.newProjectDir("sns-worker-variant-a")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.aws.java.sns.SnsClientBuildService
        import com.kelvsyc.gradle.aws.java.sns.fixtures.BuildServiceWorkerProbeTask

        val snsService = gradle.sharedServices.registerIfAbsent(
            "sns",
            SnsClientBuildService::class
        ) { }

        tasks.register<BuildServiceWorkerProbeTask>("probe") {
            service.set(snsService)
            usesService(snsService)
        }
        """.trimIndent()
    )
    return projectDir
}

private fun writeVariantBProject(): File {
    val projectDir = IntegrationTestSupport.newProjectDir("sns-worker-variant-b")
    File(projectDir, "settings.gradle.kts").writeText("")
    File(projectDir, "build.gradle.kts").writeText(
        """
        ${IntegrationTestSupport.buildscriptBlock()}

        import com.kelvsyc.gradle.aws.java.sns.fixtures.ByoClientWorkerProbeTask

        tasks.register<ByoClientWorkerProbeTask>("probe") {
            region.set("us-east-1")
        }
        """.trimIndent()
    )
    return projectDir
}
