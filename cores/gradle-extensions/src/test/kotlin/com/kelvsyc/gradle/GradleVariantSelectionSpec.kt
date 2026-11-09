package com.kelvsyc.gradle

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldNotBeEmpty
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import java.io.File
import kotlin.io.path.createTempDirectory

/**
 * Verifies which variant of this library real builds select. Once the wrapper is on Gradle 9.9 this is the only
 * coverage of the baseline variant, because no build in this repository resolves it any more.
 */
class GradleVariantSelectionSpec : FunSpec() {
    private val repo = System.getProperty("variantTest.repo").replace("\\", "/")
    private val coordinates = "com.kelvsyc.gradle:gradle-extensions:+"
    private val deprecationMessage = "Use Gradle's ProviderFactory"

    // The dynamic version must only ever be looked up in the repository holding the build under test.
    private fun repositoriesBlock(fixtureSettings: String = "") = """
        repositories {
            exclusiveContent {
                forRepository { maven(uri("$repo")) { $fixtureSettings } }
                filter { includeGroup("com.kelvsyc.gradle") }
            }
            mavenCentral()
            gradlePluginPortal()
        }
    """.trimIndent()

    private val repositories = repositoriesBlock()

    private val printClasspaths = """
        fun jars(name: String) = configurations.getByName(name).files.map { it.name }
            .filter { it.startsWith("gradle-extensions") }
        println("JARS=" + (jars("compileClasspath") + jars("runtimeClasspath")).joinToString(","))
    """.trimIndent()

    private val usage = """
        import com.kelvsyc.gradle.providers.absent
        import com.kelvsyc.gradle.providers.ofNullable
        import org.gradle.api.provider.ProviderFactory

        fun use(p: ProviderFactory) = p.ofNullable("x").orElse(p.absent)
    """.trimIndent()

    // The unique comment defeats Gradle's compiled-script cache, which would otherwise hide compiler warnings.
    private fun buildscriptConsumer() = mapOf(
        "build.gradle.kts" to """
            // ${System.nanoTime()}
            import com.kelvsyc.gradle.providers.absent
            import com.kelvsyc.gradle.providers.ofNullable

            buildscript {
                $repositories
                dependencies { classpath("$coordinates") }
            }

            println("VALUE=" + providers.ofNullable("x").get())
            println("ABSENT=" + providers.absent.isPresent)
            println("JARS=" + buildscript.configurations.getByName("classpath").files.map { it.name }
                .filter { it.startsWith("gradle-extensions") }.joinToString(","))
        """.trimIndent(),
    )

    private fun kotlinDslConsumer() = mapOf(
        "build.gradle.kts" to """
            plugins { `kotlin-dsl` }
            $repositories
            dependencies { implementation("$coordinates") }
            $printClasspaths
        """.trimIndent(),
        "src/main/kotlin/Use.kt" to usage,
    )

    private fun javaLibraryConsumer(repositoryBlock: String = repositories, extra: String = "") = mapOf(
        "build.gradle.kts" to """
            plugins {
                `java-library`
                kotlin("jvm") version "2.4.10"
            }
            $repositoryBlock
            kotlin { jvmToolchain(21) }
            dependencies {
                compileOnly(gradleApi())
                implementation("$coordinates")
            }
            $printClasspaths
            $extra
        """.trimIndent(),
        "src/main/kotlin/Use.kt" to usage,
    )

    private fun run(files: Map<String, String>, version: String, vararg arguments: String): String {
        val dir = createTempDirectory("variant-test").toFile()
        try {
            File(dir, "settings.gradle.kts").writeText("""rootProject.name = "consumer"""")
            files.forEach { (path, content) ->
                File(dir, path).apply { parentFile.mkdirs() }.writeText(content)
            }
            return GradleRunner.create()
                .withProjectDir(dir)
                .withGradleVersion(version)
                .withArguments(*arguments)
                .build()
                .output
        } finally {
            dir.deleteRecursively()
        }
    }

    private fun printed(output: String, key: String) =
        output.lineSequence().first { it.startsWith("$key=") }.removePrefix("$key=").split(",").filter(String::isNotEmpty)

    init {
        val versions = listOf("9.8.2", "9.9.0")
        val consumers = mapOf(
            "buildscript" to (::buildscriptConsumer to "help"),
            "kotlin-dsl" to (::kotlinDslConsumer to "compileKotlin"),
            "java-library" to ({ javaLibraryConsumer() } to "compileKotlin"),
        )

        versions.forEach { version ->
            val wantGradle99 = version == "9.9.0"

            consumers.forEach { (name, consumer) ->
                test("$name on $version selects the expected variant") {
                    val output = run(consumer.first(), version, consumer.second)

                    val jars = printed(output, "JARS")
                    jars.shouldNotBeEmpty()
                    jars.forEach { it.endsWith("-gradle99.jar") shouldBe wantGradle99 }
                    output.contains(deprecationMessage) shouldBe wantGradle99
                }
            }

            test("buildscript on $version can call the helpers") {
                val output = run(buildscriptConsumer(), version, "help")

                output shouldContain "VALUE=x"
                output shouldContain "ABSENT=false"
            }

            test("sources resolve on $version without ambiguity") {
                val sources = """
                    val sources = configurations.runtimeClasspath.get().incoming.artifactView {
                        withVariantReselection()
                        lenient(true)
                        attributes {
                            attribute(Category.CATEGORY_ATTRIBUTE, objects.named(Category.DOCUMENTATION))
                            attribute(DocsType.DOCS_TYPE_ATTRIBUTE, objects.named(DocsType.SOURCES))
                            attribute(Bundling.BUNDLING_ATTRIBUTE, objects.named(Bundling.EXTERNAL))
                            attribute(Usage.USAGE_ATTRIBUTE, objects.named(Usage.JAVA_RUNTIME))
                        }
                    }.files
                    println("SOURCES=" + sources.map { it.name }.filter { it.startsWith("gradle-extensions") }.joinToString(","))
                """.trimIndent()

                val output = run(javaLibraryConsumer(extra = sources), version, "help")

                val jars = printed(output, "SOURCES")
                jars.size shouldBe 1
                jars.single().endsWith("-gradle99-sources.jar") shouldBe wantGradle99
            }
        }

        versions.forEach { version ->
            test("a configuration requesting no attributes resolves on $version") {
                val detached = """
                    val detached = configurations.detachedConfiguration(dependencies.create("$coordinates"))
                    println("DETACHED=" + detached.files.map { it.name }.filter { it.startsWith("gradle-extensions") }.joinToString(","))
                """.trimIndent()

                val output = run(javaLibraryConsumer(extra = detached), version, "help")

                val jars = printed(output, "DETACHED")
                jars.size shouldBe 1
                jars.single().endsWith("-gradle99.jar") shouldBe (version == "9.9.0")
            }
        }

        test("configuration cache does not change selection") {
            val output = run(kotlinDslConsumer(), "9.9.0", "compileKotlin", "--configuration-cache")

            printed(output, "JARS").forEach { it.endsWith("-gradle99.jar") shouldBe true }
        }

        test("maven-only consumer gets the baseline") {
            val mavenOnly = repositoriesBlock(
                "metadataSources { mavenPom(); artifact(); ignoreGradleMetadataRedirection() }",
            )

            val output = run(javaLibraryConsumer(repositoryBlock = mavenOnly), "9.9.0", "help")

            val jars = printed(output, "JARS")
            jars.shouldNotBeEmpty()
            jars.forEach { it.contains("gradle99") shouldBe false }
        }
    }
}
