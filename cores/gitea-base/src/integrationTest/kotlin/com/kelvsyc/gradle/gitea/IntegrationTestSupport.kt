package com.kelvsyc.gradle.gitea

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Utilities shared by the integration-test specs in this module. See the artifactory-base module's copy for details.
 */
internal object IntegrationTestSupport {
    private val hostClasspath: String
        get() = requireNotNull(System.getProperty("integration-test.host-classpath")) {
            "integration-test.host-classpath system property not set; convention plugin not applied?"
        }

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

    fun newProjectDir(prefix: String): File =
        createTempDirectory(prefix).toFile().apply { deleteOnExit() }

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

internal sealed interface ProbeOutcome {
    data class Succeeded(val result: BuildResult) : ProbeOutcome
    data class Failed(val message: String) : ProbeOutcome
}
