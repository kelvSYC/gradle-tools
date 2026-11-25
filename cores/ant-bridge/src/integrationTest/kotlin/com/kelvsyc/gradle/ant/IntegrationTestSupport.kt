package com.kelvsyc.gradle.ant

import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Utilities shared by the integration-test specs in this module.
 *
 * The convention plugin `com.kelvsyc.internal.gradle-tools.gradle-integration-test` exposes the host component's runtime
 * classpath (plus the `integrationTest` source set output) via the `integration-test.host-classpath` system
 * property. Specs emit that classpath into the generated TestKit project's `buildscript { }` block so the
 * daemon can resolve the host component's types and the synthetic fixture classes living in this source set.
 */
internal object IntegrationTestSupport {
    private val hostClasspath: String
        get() = requireNotNull(System.getProperty("integration-test.host-classpath")) {
            "integration-test.host-classpath system property not set; convention plugin not applied?"
        }

    /** The Kotlin DSL `buildscript { }` block putting the host classpath on the build script classpath. */
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

    /** The Groovy DSL `buildscript { }` block putting the host classpath on the build script classpath. */
    fun groovyBuildscriptBlock(): String {
        val entries = hostClasspath.split(File.pathSeparator).joinToString(",\n            ") { path ->
            "'" + path.replace("\\", "\\\\") + "'"
        }
        return """
            buildscript {
                dependencies {
                    classpath files(
                        $entries
                    )
                }
            }
        """.trimIndent()
    }

    fun newProjectDir(prefix: String): File =
        createTempDirectory(prefix).toFile().apply { deleteOnExit() }

    /** Runs a build that must succeed, with [environment] added to this JVM's environment. */
    fun run(projectDir: File, vararg args: String, environment: Map<String, String> = emptyMap()): BuildResult =
        runner(projectDir, args.toList(), environment).build()

    /** Runs a build that must fail. */
    fun runFailing(projectDir: File, vararg args: String): BuildResult =
        runner(projectDir, args.toList(), emptyMap()).buildAndFail()

    private fun runner(projectDir: File, args: List<String>, environment: Map<String, String>): GradleRunner {
        val runner = GradleRunner.create()
            .withProjectDir(projectDir)
            .withArguments(args + "--stacktrace")
            .forwardOutput()
        return if (environment.isEmpty()) runner else runner.withEnvironment(System.getenv() + environment)
    }
}
