package com.kelvsyc.gradle.plugins

import com.kelvsyc.gradle.karakum.KarakumExtension
import com.kelvsyc.gradle.karakum.KarakumTask
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.findByType
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class KarakumPluginSpec : FunSpec() {
    init {
        context("KarakumPlugin - extension") {
            test("registers a karakum extension on the project") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(KarakumPlugin::class)

                project.extensions.findByType<KarakumExtension>() shouldNotBe null
            }

            test("karakumCommand convention defaults to npx karakum when no version is set") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(KarakumPlugin::class)
                val ext = project.extensions.findByType<KarakumExtension>()!!

                ext.karakumCommand.get() shouldBe listOf("npx", "karakum")
            }

            test("karakumCommand convention includes version when karakumVersion is set") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(KarakumPlugin::class)
                val ext = project.extensions.findByType<KarakumExtension>()!!

                ext.karakumVersion.set("1.5.0")

                ext.karakumCommand.get() shouldBe listOf("npx", "karakum@1.5.0")
            }
        }

        context("KarakumPlugin - task convention") {
            test("KarakumTask inherits karakumCommand convention from extension") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(KarakumPlugin::class)

                val task = project.tasks.register<KarakumTask>("gen").get()

                task.karakumCommand.get() shouldBe listOf("npx", "karakum")
            }

            test("explicit karakumCommand on task overrides extension convention") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(KarakumPlugin::class)

                val task = project.tasks.register<KarakumTask>("gen") {
                    karakumCommand.set(listOf("karakum"))
                }.get()

                task.karakumCommand.get() shouldBe listOf("karakum")
            }

            test("changing extension karakumVersion after task registration is reflected in task") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(KarakumPlugin::class)
                val ext = project.extensions.findByType<KarakumExtension>()!!
                val task = project.tasks.register<KarakumTask>("gen").get()

                ext.karakumVersion.set("2.0.0")

                task.karakumCommand.get() shouldBe listOf("npx", "karakum@2.0.0")
            }
        }
    }
}
