package com.kelvsyc.gradle.compat

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.kotest.matchers.string.shouldNotContain
import org.gradle.testkit.runner.BuildResult
import org.gradle.testkit.runner.GradleRunner
import org.gradle.util.GradleVersion
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Verifies that freshly published cores can be used on the oldest Gradle of their tier, and are refused on older Gradle.
 *
 * The consumers are build-logic projects with Gradle's Kotlin DSL plugins on their classpath. Those plugins pin
 * kotlin-stdlib strictly to the Kotlin embedded in the running Gradle, so any stdlib requirement a core publishes,
 * directly or through its dependencies, above that version fails resolution.
 */
class TierCompatibilitySpec : FunSpec() {
    private val repo = System.getProperty("compat.repo").replace("\\", "/")

    // Published cores leave third-party versions to BOMs, which consumers import themselves.
    private val platforms = System.getProperty("compat.platforms").split(",").filter(String::isNotEmpty)

    // Published cores also leave the versions of other cores to our BOM. These constraints stand in for it.
    private fun coreConstraints(configuration: String) = File(repo, "com/kelvsyc/gradle").list().orEmpty().sorted()
        .joinToString("\n") { "$configuration(\"com.kelvsyc.gradle:$it:+\")" }

    private fun platforms(configuration: String) = platforms.joinToString("\n") { "$configuration(platform(\"$it\"))" }

    // Dynamic versions must only ever be looked up in the repository holding the cores under test.
    private val repositories = """
        repositories {
            exclusiveContent {
                forRepository { maven(uri("$repo")) }
                filter { includeGroup("com.kelvsyc.gradle") }
            }
            mavenCentral()
            gradlePluginPortal()
        }
    """.trimIndent()

    private fun buildLogicConsumer(core: String, usage: String) = mapOf(
        "build.gradle.kts" to """
            import org.gradle.kotlin.dsl.support.expectedKotlinDslPluginsVersion

            plugins { `kotlin-dsl` }
            $repositories
            dependencies {
                implementation("org.gradle.kotlin:gradle-kotlin-dsl-plugins:${'$'}expectedKotlinDslPluginsVersion")
                ${platforms("implementation")}
                implementation("com.kelvsyc.gradle:$core:+")
                constraints {
                    ${coreConstraints("implementation")}
                }
            }
        """.trimIndent(),
        "src/main/kotlin/Use.kt" to usage,
    )

    private val nexusBase = buildLogicConsumer(
        "nexus-base",
        """
            import com.kelvsyc.gradle.nexus.BatchDownloadFromNexus

            fun use(task: BatchDownloadFromNexus) = task.name
        """.trimIndent(),
    )

    private val awsStsKotlinBase = buildLogicConsumer(
        "aws-sts-kotlin-base",
        """
            import com.kelvsyc.gradle.aws.kotlin.sts.StsClientBuildService

            fun use(service: StsClientBuildService) = service.parameters
        """.trimIndent(),
    )

    // Kotlin build scripts are compiled without Kotlin's metadata version check, unlike build-logic sources.
    private val awsStsKotlinBaseBuildScript = mapOf(
        "build.gradle.kts" to """
            buildscript {
                $repositories
                dependencies {
                    ${platforms("classpath")}
                    classpath("com.kelvsyc.gradle:aws-sts-kotlin-base:+")
                    constraints {
                        ${coreConstraints("classpath")}
                    }
                }
            }

            println("CLASS=" + com.kelvsyc.gradle.aws.kotlin.sts.StsClientBuildService::class.java.name)
        """.trimIndent(),
    )

    private fun runner(files: Map<String, String>, version: String, vararg args: String): Pair<File, GradleRunner> {
        val dir = createTempDirectory("compat-test").toFile()
        File(dir, "settings.gradle.kts").writeText("""rootProject.name = "consumer"""")
        files.forEach { (path, content) ->
            File(dir, path).apply { parentFile.mkdirs() }.writeText(content)
        }
        return dir to GradleRunner.create()
            .withProjectDir(dir)
            .withGradleVersion(version)
            .withArguments(*args)
    }

    /** Runs a build of [files] on Gradle [version] with [args], expecting it to succeed. */
    fun run(files: Map<String, String>, version: String, vararg args: String): BuildResult {
        val (dir, runner) = runner(files, version, *args)
        try {
            return runner.build()
        } finally {
            dir.deleteRecursively()
        }
    }

    /** Runs a build of [files] on Gradle [version] with [args], expecting it to fail. */
    fun runAndFail(files: Map<String, String>, version: String, vararg args: String): BuildResult {
        val (dir, runner) = runner(files, version, *args)
        try {
            return runner.buildAndFail()
        } finally {
            dir.deleteRecursively()
        }
    }

    init {
        val current = GradleVersion.current().version

        listOf("8.14", current).distinct().forEach { version ->
            test("build-logic resolves nexus-base on $version") {
                run(nexusBase, version, "compileKotlin")
            }
        }

        // The AWS SDK ships Kotlin 2.4 metadata, which only the Kotlin 2.3 compiler of Gradle 9.4 and later can read.
        // Kotlin-source build-logic therefore needs Gradle 9.4, while build scripts work from the tier's 9.0.
        listOf("9.4.1", current).distinct().forEach { version ->
            test("build-logic resolves aws-sts-kotlin-base on $version") {
                run(awsStsKotlinBase, version, "compileKotlin")
            }
        }

        listOf("9.0.0", current).distinct().forEach { version ->
            test("build script resolves aws-sts-kotlin-base on $version") {
                val output = run(awsStsKotlinBaseBuildScript, version, "help").output

                output shouldContain "CLASS=com.kelvsyc.gradle.aws.kotlin.sts.StsClientBuildService"
            }
        }

        test("aws-sts-kotlin-base is rejected on 8.14 at resolution") {
            val output = runAndFail(awsStsKotlinBase, "8.14", "compileKotlin").output

            output shouldContain "org.gradle.plugin.api-version"
            output shouldNotContain "NoClassDefFoundError"
        }
    }
}
