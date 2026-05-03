package com.kelvsyc.gradle.jfrog

import com.kelvsyc.gradle.plugins.JFrogCliPlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class AddGitInfoToBuildSpec : FunSpec() {
    init {
        context("AddGitInfoToBuild - property configuration") {
            test("buildName can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<AddGitInfoToBuild>("myTask").get()

                task.buildName.set("my-build")

                task.buildName.get() shouldBe "my-build"
            }

            test("buildNumber can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<AddGitInfoToBuild>("myTask").get()

                task.buildNumber.set("42")

                task.buildNumber.get() shouldBe "42"
            }

            test("jfCommand can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<AddGitInfoToBuild>("myTask").get()

                task.jfCommand.set("/usr/local/bin/jf")

                task.jfCommand.get() shouldBe "/usr/local/bin/jf"
            }
        }

        context("AddGitInfoToBuild - plugin convention") {
            test("jfCommand is absent without plugin applied") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<AddGitInfoToBuild>("myTask").get()

                task.jfCommand.isPresent shouldBe false
            }

            test("plugin applies and wires jfCommand convention without error") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(JFrogCliPlugin::class)

                project.tasks.register<AddGitInfoToBuild>("myTask").get()
            }

            test("explicit jfCommand overrides the convention set by the plugin") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(JFrogCliPlugin::class)

                val task = project.tasks.register<AddGitInfoToBuild>("myTask") {
                    jfCommand.set("/custom/jf")
                }.get()

                task.jfCommand.get() shouldBe "/custom/jf"
            }
        }
    }
}
