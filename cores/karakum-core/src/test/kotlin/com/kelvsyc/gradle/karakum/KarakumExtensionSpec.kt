package com.kelvsyc.gradle.karakum

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.newInstance
import org.gradle.testfixtures.ProjectBuilder

class KarakumExtensionSpec : FunSpec() {
    init {
        context("KarakumExtension - useNpx()") {
            test("sets command to npx karakum when no version is set") {
                val project = ProjectBuilder.builder().build()
                val ext = project.objects.newInstance<KarakumExtension>()

                ext.useNpx()

                ext.karakumCommand.get() shouldBe listOf("npx", "karakum")
            }

            test("sets command to npx karakum@version when version is set") {
                val project = ProjectBuilder.builder().build()
                val ext = project.objects.newInstance<KarakumExtension>()
                ext.karakumVersion.set("1.2.3")

                ext.useNpx()

                ext.karakumCommand.get() shouldBe listOf("npx", "karakum@1.2.3")
            }
        }

        context("KarakumExtension - useNpx(npxBinary)") {
            test("sets command using the provided npx binary path without version") {
                val project = ProjectBuilder.builder().build()
                val ext = project.objects.newInstance<KarakumExtension>()
                val npxFile = project.objects.fileProperty().also {
                    it.set(project.file("/managed/node/bin/npx"))
                }

                ext.useNpx(npxFile)

                ext.karakumCommand.get() shouldBe listOf("/managed/node/bin/npx", "karakum")
            }

            test("sets command using the provided npx binary path with version") {
                val project = ProjectBuilder.builder().build()
                val ext = project.objects.newInstance<KarakumExtension>()
                ext.karakumVersion.set("2.0.0")
                val npxFile = project.objects.fileProperty().also {
                    it.set(project.file("/managed/node/bin/npx"))
                }

                ext.useNpx(npxFile)

                ext.karakumCommand.get() shouldBe listOf("/managed/node/bin/npx", "karakum@2.0.0")
            }
        }

        context("KarakumExtension - useNodeModules()") {
            test("sets command to node_modules/.bin/karakum") {
                val project = ProjectBuilder.builder().build()
                val ext = project.objects.newInstance<KarakumExtension>()

                ext.useNodeModules()

                ext.karakumCommand.get() shouldBe listOf("node_modules/.bin/karakum")
            }
        }

        context("KarakumExtension - useNodeModules(nodeBinary)") {
            test("sets command using the provided node binary path") {
                val project = ProjectBuilder.builder().build()
                val ext = project.objects.newInstance<KarakumExtension>()
                val nodeFile = project.objects.fileProperty().also {
                    it.set(project.file("/managed/node/bin/node"))
                }

                ext.useNodeModules(nodeFile)

                ext.karakumCommand.get() shouldBe
                    listOf("/managed/node/bin/node", "node_modules/.bin/karakum")
            }
        }
    }
}
