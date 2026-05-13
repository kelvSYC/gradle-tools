package com.kelvsyc.gradle.aws.java.sns

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Utilities shared by the integration-test specs in this module.
 *
 * The convention plugin `com.kelvsyc.internal.gradle-integration-test` exposes the host component's runtime
 * classpath (plus the `integrationTest` source set output) via the `integration-test.host-classpath` system
 * property. Specs emit that classpath into the generated TestKit project's `buildscript { }` block so the
 * daemon can resolve the host component's types and the synthetic fixture classes living in this source set.
 */
internal object IntegrationTestSupport {
    private val hostClasspath: String
        get() = requireNotNull(System.getProperty("integration-test.host-classpath")) {
            "integration-test.host-classpath system property not set; convention plugin not applied?"
        }

    /**
     * Returns a Kotlin DSL `buildscript { }` block that puts the host component's runtime classpath onto the
     * generated project's buildscript classpath, so type references in the generated `build.gradle.kts`
     * resolve at script-compile time.
     */
    fun buildscriptBlock(): String {
        val entries = hostClasspath.split(File.pathSeparator).joinToString(",\n            ") { path ->
            "\"" + path.replace("\\", "\\\\") + "\""
        }
        return """
            buildscript {
                dependencies {
                    classpath(files(
                        $entries
                    ))
                }
            }
        """.trimIndent()
    }

    /** Creates a fresh temp directory whose lifetime spans the spec invocation. */
    fun newProjectDir(prefix: String): File =
        createTempDirectory(prefix).toFile().apply { deleteOnExit() }

    /**
     * Runs a TestKit invocation with the supplied arguments, capturing whether it succeeded or failed and
     * the relevant outcome details. Useful for observation-style probes where either result is a meaningful
     * finding and we want to encode the *current* behavior so future changes in either direction surface.
     */
    fun runProbe(projectDir: File, vararg args: String): ProbeOutcome {
        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(*args)
            .forwardOutput()
        return runCatching { runner.build() }.fold(
            onSuccess = { ProbeOutcome.Succeeded(it) },
            onFailure = { ProbeOutcome.Failed(it.message.orEmpty()) }
        )
    }
}

/** Outcome of a TestKit probe: either it built or it failed with a captured message. */
internal sealed interface ProbeOutcome {
    /** The TestKit invocation built successfully. */
    data class Succeeded(val result: BuildResult) : ProbeOutcome

    /** The TestKit invocation failed with the captured error message from the Gradle reporter. */
    data class Failed(val message: String) : ProbeOutcome
}
