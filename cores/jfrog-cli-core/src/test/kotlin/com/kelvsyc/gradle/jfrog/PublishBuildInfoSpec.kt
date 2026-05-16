package com.kelvsyc.gradle.jfrog

import com.kelvsyc.gradle.clients.CredentialReference
import com.kelvsyc.gradle.plugins.JFrogCliPlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class PublishBuildInfoSpec : FunSpec() {
    init {
        context("PublishBuildInfo - property configuration") {
            test("buildName can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<PublishBuildInfo>("myTask").get()

                task.buildName.set("my-build")

                task.buildName.get() shouldBe "my-build"
            }

            test("buildNumber can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<PublishBuildInfo>("myTask").get()

                task.buildNumber.set("42")

                task.buildNumber.get() shouldBe "42"
            }

            test("serverUrl can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<PublishBuildInfo>("myTask").get()

                task.serverUrl.set("https://artifactory.example.com")

                task.serverUrl.get() shouldBe "https://artifactory.example.com"
            }

            test("accessTokenRef can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<PublishBuildInfo>("myTask").get()

                task.accessTokenRef.set(CredentialReference.EnvironmentVariable("JFROG_ACCESS_TOKEN"))

                task.accessTokenRef.get() shouldBe CredentialReference.EnvironmentVariable("JFROG_ACCESS_TOKEN")
            }

            test("envExcludePatterns can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<PublishBuildInfo>("myTask").get()

                task.envExcludePatterns.addAll(listOf("*SECRET*", "*PASSWORD*"))

                task.envExcludePatterns.get() shouldBe listOf("*SECRET*", "*PASSWORD*")
            }

            test("envExcludePatterns default to empty") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<PublishBuildInfo>("myTask").get()

                task.envExcludePatterns.get() shouldBe emptyList()
            }
        }

        context("PublishBuildInfo - plugin convention") {
            test("jfCommand is absent without plugin applied") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<PublishBuildInfo>("myTask").get()

                task.jfCommand.isPresent shouldBe false
            }

            test("plugin applies and wires jfCommand convention without error") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(JFrogCliPlugin::class)

                project.tasks.register<PublishBuildInfo>("myTask").get()
            }

            test("explicit jfCommand overrides the convention set by the plugin") {
                val project = ProjectBuilder.builder().build()
                project.pluginManager.apply(JFrogCliPlugin::class)

                val task = project.tasks.register<PublishBuildInfo>("myTask") {
                    jfCommand.set("/custom/jf")
                }.get()

                task.jfCommand.get() shouldBe "/custom/jf"
            }
        }
    }
}
