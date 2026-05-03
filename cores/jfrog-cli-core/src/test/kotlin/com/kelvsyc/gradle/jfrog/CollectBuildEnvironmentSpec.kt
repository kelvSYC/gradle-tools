package com.kelvsyc.gradle.jfrog

import com.kelvsyc.gradle.plugins.JFrogCliPlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class CollectBuildEnvironmentSpec : FunSpec() {
    init {
        context("CollectBuildEnvironment - property configuration") {
            test("buildName can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<CollectBuildEnvironment>("myTask").get()

                task.buildName.set("my-build")

                task.buildName.get() shouldBe "my-build"
            }

            test("buildNumber can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<CollectBuildEnvironment>("myTask").get()

                task.buildNumber.set("42")

                task.buildNumber.get() shouldBe "42"
            }

            test("includePatterns can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<CollectBuildEnvironment>("myTask").get()

                task.includePatterns.addAll(listOf("MY_APP_*", "CI_*"))

                task.includePatterns.get() shouldBe listOf("MY_APP_*", "CI_*")
            }

            test("excludePatterns can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<CollectBuildEnvironment>("myTask").get()

                task.excludePatterns.addAll(listOf("*SECRET*", "*PASSWORD*"))

                task.excludePatterns.get() shouldBe listOf("*SECRET*", "*PASSWORD*")
            }

            test("includePatterns default to empty") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<CollectBuildEnvironment>("myTask").get()

                task.includePatterns.get() shouldBe emptyList()
            }

            test("excludePatterns default to empty") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<CollectBuildEnvironment>("myTask").get()

                task.excludePatterns.get() shouldBe emptyList()
            }
        }

        context("CollectBuildEnvironment - plugin convention") {
            test("jfCommand is absent without plugin applied") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<CollectBuildEnvironment>("myTask").get()

                task.jfCommand.isPresent shouldBe false
            }

            test("plugin applies and wires jfCommand convention without error") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(JFrogCliPlugin::class)

                project.tasks.register<CollectBuildEnvironment>("myTask").get()
            }

            test("explicit jfCommand overrides the convention set by the plugin") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(JFrogCliPlugin::class)

                val task = project.tasks.register<CollectBuildEnvironment>("myTask") {
                    jfCommand.set("/custom/jf")
                }.get()

                task.jfCommand.get() shouldBe "/custom/jf"
            }
        }
    }
}
