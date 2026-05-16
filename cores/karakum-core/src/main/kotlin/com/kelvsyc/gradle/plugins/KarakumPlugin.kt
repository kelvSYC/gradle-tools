package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.karakum.KarakumExtension
import com.kelvsyc.gradle.karakum.KarakumTask
import org.gradle.api.Plugin
import org.gradle.api.Project
import org.gradle.kotlin.dsl.withType
import org.jetbrains.kotlin.gradle.dsl.KotlinJsProjectExtension
import org.jetbrains.kotlin.gradle.dsl.KotlinMultiplatformExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootExtension
import org.jetbrains.kotlin.gradle.targets.js.nodejs.NodeJsRootPlugin

/**
 * Gradle plugin that integrates Karakum TypeScript-to-Kotlin external-declaration generation
 * into the build lifecycle.
 *
 * When applied, the plugin:
 * 1. Registers a `karakum` [KarakumExtension] with a default `npx karakum` invocation command.
 * 2. Propagates the extension convention to all [KarakumTask] instances.
 * 3. Reacts to `kotlin("js")` and `kotlin("multiplatform")` plugins: registers a generation task,
 *    wires its output into the appropriate Kotlin source set, and upgrades the command convention
 *    to use the KMP-managed Node.js `npx` binary when the Kotlin Node.js plugin is applied.
 */
class KarakumPlugin : Plugin<Project> {
    override fun apply(project: Project) {
        val extension = project.extensions.create("karakum", KarakumExtension::class.java)

        extension.karakumCommand.convention(
            extension.karakumVersion.map { listOf("npx", "karakum@$it") }
                .orElse(listOf("npx", "karakum"))
        )

        project.tasks.withType<KarakumTask>().configureEach {
            karakumCommand.convention(extension.karakumCommand)
        }

        project.pluginManager.withPlugin("org.jetbrains.kotlin.js") {
            wireKotlinJs(project, extension)
        }

        project.pluginManager.withPlugin("org.jetbrains.kotlin.multiplatform") {
            wireKotlinMultiplatform(project, extension)
        }
    }

    private fun wireKotlinJs(project: Project, extension: KarakumExtension) {
        val kotlin = project.extensions.getByType(KotlinJsProjectExtension::class.java)
        upgradeToManagedNpx(project, extension)

        val generateTask = project.tasks.register("generateKotlinExternals", KarakumTask::class.java) {
            outputDirectory.convention(project.layout.buildDirectory.dir("generated/karakum/main"))
        }
        kotlin.sourceSets.getByName("main").kotlin.srcDir(generateTask.map { it.outputDirectory })
    }

    private fun wireKotlinMultiplatform(project: Project, extension: KarakumExtension) {
        val kotlin = project.extensions.getByType(KotlinMultiplatformExtension::class.java)
        upgradeToManagedNpx(project, extension)

        val generateTask = project.tasks.register("generateKotlinJsExternals", KarakumTask::class.java) {
            outputDirectory.convention(project.layout.buildDirectory.dir("generated/karakum/jsMain"))
        }
        kotlin.sourceSets.matching { it.name == "jsMain" }.configureEach {
            this.kotlin.srcDir(generateTask.map { it.outputDirectory })
        }
    }

    private fun upgradeToManagedNpx(project: Project, extension: KarakumExtension) {
        project.rootProject.plugins.withType<NodeJsRootPlugin> {
            val nodeJsRoot = project.rootProject.extensions.getByType(NodeJsRootExtension::class.java)
            val managedNpx = project.layout.file(
                nodeJsRoot.nodeJsSetupTaskProvider.flatMap { task ->
                    task.destinationProvider.map { it.asFile.resolve("bin/npx") }
                }
            )
            extension.karakumCommand.convention(
                managedNpx.zip(
                    extension.karakumVersion.map { "karakum@$it" }.orElse("karakum")
                ) { npx, pkg -> listOf(npx.asFile.absolutePath, pkg) }
            )
        }
    }
}
