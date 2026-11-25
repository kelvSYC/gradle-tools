package com.kelvsyc.gradle.ant

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.types.shouldBeInstanceOf
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

/**
 * Probe: configuration-cache round-trip of `AntRuntimeService`, which has no parameters.
 */
class BuildServiceConfigurationCacheSpec : FunSpec({
    test("AntRuntimeService with no parameters survives config-cache round-trip") {
        val projectDir = IntegrationTestSupport.newProjectDir("ant-runtime-config-cache")
        File(projectDir, "settings.gradle.kts").writeText("")
        File(projectDir, "build.gradle.kts").writeText(
            """
            ${IntegrationTestSupport.buildscriptBlock()}

            import com.kelvsyc.gradle.ant.internal.AntRuntimeService
            import com.kelvsyc.gradle.ant.fixtures.AntRuntimeServiceProbeTask

            val runtime = gradle.sharedServices.registerIfAbsent("antBridgeRuntime", AntRuntimeService::class) {}

            tasks.register<AntRuntimeServiceProbeTask>("probe") {
                service.set(runtime)
                usesService(runtime)
            }
            """.trimIndent()
        )

        val first = IntegrationTestSupport.runProbe(projectDir, "probe", "--configuration-cache", "--stacktrace")
        first.shouldBeInstanceOf<ProbeOutcome.Succeeded>().result.task(":probe")?.outcome shouldBe TaskOutcome.SUCCESS

        val second = IntegrationTestSupport.runProbe(projectDir, "probe", "--configuration-cache", "--stacktrace")
        val secondSucceeded = second.shouldBeInstanceOf<ProbeOutcome.Succeeded>()
        secondSucceeded.result.task(":probe")?.outcome shouldBe TaskOutcome.SUCCESS
        secondSucceeded.result.output shouldContain "Configuration cache entry reused"
    }
})
