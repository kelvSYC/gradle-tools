package com.kelvsyc.gradle.jfrog

import com.kelvsyc.gradle.plugins.JFrogCliPlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class ScanBuildSpec : FunSpec() {
    init {
        context("ScanBuild - property configuration") {
            test("buildName can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<ScanBuild>("myTask").get()

                task.buildName.set("my-build")

                task.buildName.get() shouldBe "my-build"
            }

            test("buildNumber can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<ScanBuild>("myTask").get()

                task.buildNumber.set("42")

                task.buildNumber.get() shouldBe "42"
            }

            test("serverUrl can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<ScanBuild>("myTask").get()

                task.serverUrl.set("https://artifactory.example.com")

                task.serverUrl.get() shouldBe "https://artifactory.example.com"
            }

            test("accessToken can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<ScanBuild>("myTask").get()

                task.accessToken.set("mytoken")

                task.accessToken.get() shouldBe "mytoken"
            }

            test("failBuild can be set to true") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<ScanBuild>("myTask").get()

                task.failBuild.set(true)

                task.failBuild.get() shouldBe true
            }

            test("failBuild can be set to false") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<ScanBuild>("myTask").get()

                task.failBuild.set(false)

                task.failBuild.get() shouldBe false
            }
        }

        context("ScanBuild - plugin convention") {
            test("jfCommand is absent without plugin applied") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<ScanBuild>("myTask").get()

                task.jfCommand.isPresent shouldBe false
            }

            test("plugin applies and wires jfCommand convention without error") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(JFrogCliPlugin::class)

                project.tasks.register<ScanBuild>("myTask").get()
            }

            test("explicit jfCommand overrides the convention set by the plugin") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(JFrogCliPlugin::class)

                val task = project.tasks.register<ScanBuild>("myTask") {
                    jfCommand.set("/custom/jf")
                }.get()

                task.jfCommand.get() shouldBe "/custom/jf"
            }
        }
    }
}
