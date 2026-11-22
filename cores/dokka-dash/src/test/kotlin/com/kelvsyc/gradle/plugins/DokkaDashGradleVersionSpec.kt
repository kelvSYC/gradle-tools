package com.kelvsyc.gradle.plugins

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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
abstract class DokkaDashGradleVersionSpec : FunSpec() {
    // One subdirectory per Gradle version.
    private val testKitRoot = File(System.getProperty("gradleVersionTest.testKitDir"))

    private val settings = """rootProject.name = "consumer""""

    private val kotlinBuild = """
        import com.kelvsyc.gradle.dokka.GenerateDashDocset

        plugins { id("com.kelvsyc.gradle.dokka-dash") }

        repositories { mavenCentral() }

        tasks.register<GenerateDashDocset>("docset") {
            dokkaOutputDirectory.set(layout.projectDirectory.dir("dokka"))
            docsetName.set("x")
            bundleIdentifier.set("com.example.x")
            outputDirectory.set(layout.buildDirectory.dir("d"))
        }
    """.trimIndent()

    private val groovyBuild = """
        import com.kelvsyc.gradle.dokka.GenerateDashDocset

        plugins { id 'com.kelvsyc.gradle.dokka-dash' }

        repositories { mavenCentral() }

        tasks.register('docset', GenerateDashDocset) {
            dokkaOutputDirectory.set(layout.projectDirectory.dir('dokka'))
            docsetName.set('x')
            bundleIdentifier.set('com.example.x')
            outputDirectory.set(layout.buildDirectory.dir('d'))
        }
    """.trimIndent()

    private val builds = mapOf(
        "kotlin" to ("settings.gradle.kts" to "build.gradle.kts" to kotlinBuild),
        "groovy" to ("settings.gradle" to "build.gradle" to groovyBuild),
    )

    // Every test that runs on each Gradle version.
    protected fun versionTests(version: String) {
        builds.forEach { (dsl, build) ->
            test("$dsl on $version generates a docset") {
                val (fileNames, script) = build
                val dir = createTempDirectory("dash-version-test").toFile()
                try {
                    File(dir, fileNames.first).writeText(settings)
                    File(dir, fileNames.second).writeText(script)
                    File(dir, "dokka").mkdirs()
                    File(dir, "dokka/index.html").writeText("<html></html>")

                    val result = GradleRunner.create()
                        .withProjectDir(dir)
                        .withPluginClasspath()
                        .withGradleVersion(version)
                        .withTestKitDir(File(testKitRoot, version))
                        .withArguments("docset")
                        .build()

                    result.task(":docset")?.outcome shouldBe TaskOutcome.SUCCESS
                    File(dir, "build/d/x.docset/Contents/Resources/docSet.dsidx").exists() shouldBe true
                } finally {
                    dir.deleteRecursively()
                }
            }
        }
    }
}
