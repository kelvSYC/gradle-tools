package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.ant.TestAnt
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Verifies the range of Gradle versions this plugin promises in its README, using real Gradle distributions.
 *
 * Each subclass covers one Gradle version, so that Gradle forks the subclasses into parallel test JVMs and the versions'
 * distribution download, daemon start and dependency resolution overlap. A subclass's TestKit directory is its own, as
 * TestKit directories must not be shared between concurrent builds.
 */
abstract class AntBridgeGradleVersionSpec : FunSpec() {
    // One subdirectory per Gradle version.
    private val testKitRoot = File(System.getProperty("gradleVersionTest.testKitDir"))

    private val settings = """rootProject.name = "consumer""""

    // The Ant jars the test task already resolved, as a file dependency in place of the default module dependencies:
    // no Maven Central requests from each version's fresh TestKit directory (see TestAnt.useIn).
    private val antJars = TestAnt.classpathLiteral

    private val kotlinBuild = """
        import com.kelvsyc.gradle.ant.AntWork
        import com.kelvsyc.gradle.providers.getting

        plugins { id("com.kelvsyc.gradle.ant-bridge") }

        dependencies { antBridge(files($antJars)) }
        configurations.antBridgeClasspath { exclude(group = "org.apache.ant") }

        val greeting = antBridge.properties { file("build.properties") }.getting("greeting")

        tasks.register<AntWork>("hello") {
            ant { task("echo", "message" to greeting.map { "GREETING=" + it }) }
        }
    """.trimIndent()

    private val groovyBuild = """
        import com.kelvsyc.gradle.ant.AntWork

        plugins { id 'com.kelvsyc.gradle.ant-bridge' }

        dependencies { antBridge files($antJars) }
        configurations.antBridgeClasspath.exclude(group: 'org.apache.ant')

        def greeting = antBridge.properties { file('build.properties') }.map { it['greeting'] }

        tasks.register('hello', AntWork) {
            record {
                echo(message: greeting.map { 'GREETING=' + it })
            }
        }
    """.trimIndent()

    private val builds = mapOf(
        "kotlin" to ("settings.gradle.kts" to "build.gradle.kts" to kotlinBuild),
        "groovy" to ("settings.gradle" to "build.gradle" to groovyBuild),
    )

    // Every test that runs on each Gradle version.
    protected fun versionTests(version: String) {
        builds.forEach { (dsl, build) ->
            test("$dsl on $version loads properties and runs an AntWork echo") {
                val (fileNames, script) = build
                val dir = createTempDirectory("ant-bridge-version-test").toFile()
                try {
                    File(dir, fileNames.first).writeText(settings)
                    File(dir, fileNames.second).writeText(script)
                    File(dir, "build.properties").writeText("greeting=hello\n")

                    val result = GradleRunner.create()
                        .withProjectDir(dir)
                        .withPluginClasspath()
                        .withGradleVersion(version)
                        .withTestKitDir(File(testKitRoot, version))
                        .withArguments("hello", "--configuration-cache", "--stacktrace")
                        .forwardOutput()
                        .build()

                    result.task(":hello")?.outcome shouldBe TaskOutcome.SUCCESS
                    result.output shouldContain "GREETING=hello"
                } finally {
                    dir.deleteRecursively()
                }
            }
        }
    }
}
