package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempdir
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import org.gradle.testkit.runner.GradleRunner
import org.gradle.testkit.runner.TaskOutcome
import java.io.File

class ClientsBasePluginSpec : FunSpec() {
    init {
        test("Apply to project - extension created in project") {
            val projectDir = tempdir()
            val projectFile = File(projectDir, "build.gradle.kts")
            projectFile.writeText("""
                plugins {
                    id("com.kelvsyc.gradle.clients-base")
                    id("jacoco-testkit-coverage")
                }
                
                tasks.register("printOutput") {
                    doLast {
                        if (project.extensions.findByType<${ClientsBaseExtension::class.qualifiedName}>() == null) {
                            throw GradleException("Cannot find extension with type ${ClientsBaseExtension::class.simpleName}")
                        }
                        if (project.extensions.findByName("${ClientsBasePlugin.EXTENSION_NAME}") == null) {
                            throw GradleException("Cannot find extension with name ${ClientsBasePlugin.EXTENSION_NAME}")
                        }
                    }
                }
            """.trimIndent())

            val result = GradleRunner.create().apply {
                withPluginClasspath()
                withProjectDir(projectDir)
                // FIXME https://github.com/gradle/gradle/issues/25412 - TestKit has trouble handling inline methods
                withArguments(buildList {
                    add("-Dorg.gradle.kotlin.dsl.scriptCompilationAvoidance=false")
                    add("printOutput")
                })
            }.build()

            result.task(":printOutput")?.outcome.shouldBe(TaskOutcome.SUCCESS)
        }

        test("Apply to settings - extension created in project") {
            val projectDir = tempdir()
            val settingsFile = File(projectDir, "settings.gradle.kts")
            val projectFile = File(projectDir, "build.gradle.kts")
            settingsFile.writeText("""
                plugins {
                    id("com.kelvsyc.gradle.clients-base")
                    id("jacoco-testkit-coverage")
                }
            """.trimIndent())
            projectFile.writeText("""
                tasks.register("printOutput") {
                    doLast {
                        if (project.extensions.findByType<${ClientsBaseExtension::class.qualifiedName}>() == null) {
                            throw GradleException("Cannot find extension with type ${ClientsBaseExtension::class.simpleName}")
                        }
                        if (project.extensions.findByName("${ClientsBasePlugin.EXTENSION_NAME}") == null) {
                            throw GradleException("Cannot find extension with name ${ClientsBasePlugin.EXTENSION_NAME}")
                        }
                    }
                }
            """.trimIndent())

            val result = GradleRunner.create().apply {
                withPluginClasspath()
                withProjectDir(projectDir)
                // FIXME https://github.com/gradle/gradle/issues/25412 - TestKit has trouble handling inline methods
                withArguments(buildList {
                    add("-Dorg.gradle.kotlin.dsl.scriptCompilationAvoidance=false")
                    add("printOutput")
                })
            }.build()

            result.task(":printOutput")?.outcome.shouldBe(TaskOutcome.SUCCESS)
        }

        test("Apply to settings - extension created in settings") {
            val projectDir = tempdir()
            val settingsFile = File(projectDir, "settings.gradle.kts")
            val projectFile = File(projectDir, "build.gradle.kts")
            settingsFile.writeText("""
                plugins {
                    id("com.kelvsyc.gradle.clients-base")
                    id("jacoco-testkit-coverage")
                }
                logger.lifecycle("settings extension by type: {}", extensions.findByType<${ClientsBaseExtension::class.qualifiedName}>() != null)
                logger.lifecycle("settings extension by name: {}", extensions.findByName("${ClientsBasePlugin.EXTENSION_NAME}") != null)
            """.trimIndent())
            projectFile.writeText("""
                tasks.register("doNothing")
            """.trimIndent())

            val result = GradleRunner.create().apply {
                withPluginClasspath()
                withProjectDir(projectDir)
                // FIXME https://github.com/gradle/gradle/issues/25412 - TestKit has trouble handling inline methods
                withArguments(buildList {
                    add("-Dorg.gradle.kotlin.dsl.scriptCompilationAvoidance=false")
                    add("doNothing")
                })
            }.build()

            result.output.shouldContain("settings extension by type: true")
            result.output.shouldContain("settings extension by name: true")
        }

        test("Apply to init - extension created in project") {
            val projectDir = tempdir()
            val runner = GradleRunner.create().apply {
                withPluginClasspath()
            }
            val initFile = File(projectDir, "init.gradle.kts")
            val projectFile = File(projectDir, "build.gradle.kts")
            initFile.writeText("""
                initscript {
                    dependencies {
                        classpath(files(${runner.pluginClasspath.map { "\"${it.absolutePath}\"" }.joinToString()}))
                    }
                }
                
                apply<${ClientsBasePlugin::class.qualifiedName}>()
            """.trimIndent())
            projectFile.writeText("""
                tasks.register("printOutput") {
                    doLast {
                        if (project.extensions.findByName("${ClientsBasePlugin.EXTENSION_NAME}") == null) {
                            throw GradleException("Cannot find extension with name ${ClientsBasePlugin.EXTENSION_NAME}")
                        }
                    }
                }
            """.trimIndent())

            val result = runner.apply {
                withProjectDir(projectDir)
                // FIXME https://github.com/gradle/gradle/issues/25412 - TestKit has trouble handling inline methods
                withArguments(buildList {
                    add("-Dorg.gradle.kotlin.dsl.scriptCompilationAvoidance=false")
                    add("--init-script=${initFile.absolutePath}")
                    add("printOutput")
                })
            }.build()

            result.task(":printOutput")?.outcome.shouldBe(TaskOutcome.SUCCESS)
        }

        test("Apply to init - extension created in settings") {
            val projectDir = tempdir()
            val runner = GradleRunner.create().apply {
                withPluginClasspath()
            }
            val initFile = File(projectDir, "init.gradle.kts")
            val settingsFile = File(projectDir, "settings.gradle.kts")
            val projectFile = File(projectDir, "build.gradle.kts")
            initFile.writeText("""
                initscript {
                    dependencies {
                        classpath(files(${runner.pluginClasspath.map { "\"${it.absolutePath}\"" }.joinToString()}))
                    }
                }
                
                apply<${ClientsBasePlugin::class.qualifiedName}>()
                apply<io.github.gmazzo.gradle.testkit.jacoco.JacocoTestKitReportCoveragePlugin>()
            """.trimIndent())
            settingsFile.writeText("""
                logger.lifecycle("settings extension by name: {}", extensions.findByName("${ClientsBasePlugin.EXTENSION_NAME}") != null)
            """.trimIndent())
            projectFile.writeText("""
                tasks.register("doNothing")
            """.trimIndent())

            val result = runner.apply {
                withProjectDir(projectDir)
                // FIXME https://github.com/gradle/gradle/issues/25412 - TestKit has trouble handling inline methods
                withArguments(buildList {
                    add("-Dorg.gradle.kotlin.dsl.scriptCompilationAvoidance=false")
                    add("--init-script=${initFile.absolutePath}")
                    add("doNothing")
                })
            }.build()

            result.output.shouldContain("settings extension by name: true")
        }

        test("Apply to init - extension created in init") {
            val projectDir = tempdir()
            val runner = GradleRunner.create().apply {
                withPluginClasspath()
            }
            val initFile = File(projectDir, "init.gradle.kts")
            val projectFile = File(projectDir, "build.gradle.kts")
            initFile.writeText("""
                initscript {
                    dependencies {
                        classpath(files(${runner.pluginClasspath.map { "\"${it.absolutePath}\"" }.joinToString()}))
                    }
                }
                
                apply<${ClientsBasePlugin::class.qualifiedName}>()
                apply<io.github.gmazzo.gradle.testkit.jacoco.JacocoTestKitReportCoveragePlugin>()
                logger.lifecycle("init extension by type: {}", extensions.findByType<${ClientsBaseExtension::class.qualifiedName}>() != null)
                logger.lifecycle("init extension by name: {}", extensions.findByName("${ClientsBasePlugin.EXTENSION_NAME}") != null)
            """.trimIndent())
            projectFile.writeText("""
                tasks.register("doNothing")
            """.trimIndent())

            val result = runner.apply {
                withProjectDir(projectDir)
                // FIXME https://github.com/gradle/gradle/issues/25412 - TestKit has trouble handling inline methods
                withArguments(buildList {
                    add("-Dorg.gradle.kotlin.dsl.scriptCompilationAvoidance=false")
                    add("--init-script=${initFile.absolutePath}")
                    add("doNothing")
                })
            }.build()

            result.output.shouldContain("init extension by type: true")
            result.output.shouldContain("init extension by name: true")
        }

        test("Apply to project and settings") {
            val projectDir = tempdir()
            val settingsFile = File(projectDir, "settings.gradle.kts")
            val projectFile = File(projectDir, "build.gradle.kts")
            settingsFile.writeText("""
                plugins {
                    id("com.kelvsyc.gradle.clients-base")
                    id("jacoco-testkit-coverage")
                }
            """.trimIndent())
            projectFile.writeText("""
                plugins {
                    id("com.kelvsyc.gradle.clients-base")
                    id("jacoco-testkit-coverage")
                }
                
                tasks.register("doNothing")
            """.trimIndent())

            val result = GradleRunner.create().apply {
                withPluginClasspath()
                withProjectDir(projectDir)
                // FIXME https://github.com/gradle/gradle/issues/25412 - TestKit has trouble handling inline methods
                withArguments(buildList {
                    add("-Dorg.gradle.kotlin.dsl.scriptCompilationAvoidance=false")
                    add("doNothing")
                })
            }.build()

            result.task(":doNothing")?.outcome.shouldBe(TaskOutcome.UP_TO_DATE)
        }
    }
}
