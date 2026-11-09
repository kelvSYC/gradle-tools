package com.kelvsyc.gradle.plugins

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import org.gradle.util.GradleVersion
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Verifies the range of Gradle versions this plugin promises in its README, using real Gradle distributions.
 *
 * The `ADDITIONAL_GRADLE_VERSIONS` environment variable adds comma-separated versions, such as a nightly.
 */
class DokkaDashGradleVersionSpec : FunSpec() {
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

    private fun additionalGradleVersions() =
        System.getenv("ADDITIONAL_GRADLE_VERSIONS")?.split(",")?.map(String::trim)?.filter(String::isNotEmpty).orEmpty()

    init {
        val versions = (listOf("8.14", "9.1.0", "9.8.2", GradleVersion.current().version) + additionalGradleVersions())
            .distinct()
        val builds = mapOf(
            "kotlin" to ("settings.gradle.kts" to "build.gradle.kts" to kotlinBuild),
            "groovy" to ("settings.gradle" to "build.gradle" to groovyBuild),
        )

        versions.forEach { version ->
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
}
