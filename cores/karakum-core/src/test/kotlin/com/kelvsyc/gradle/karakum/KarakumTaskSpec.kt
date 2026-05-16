package com.kelvsyc.gradle.karakum

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class KarakumTaskSpec : FunSpec() {
    init {
        context("KarakumTask - property configuration") {
            test("karakumCommand can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<KarakumTask>("gen").get()

                task.karakumCommand.set(listOf("npx", "karakum@1.0.0"))

                task.karakumCommand.get() shouldBe listOf("npx", "karakum@1.0.0")
            }

            test("outputDirectory can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<KarakumTask>("gen").get()
                val dir = project.layout.buildDirectory.dir("generated/karakum").get()

                task.outputDirectory.set(dir)

                task.outputDirectory.get() shouldBe dir
            }

            test("configFile is absent by default") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<KarakumTask>("gen").get()

                task.configFile.isPresent shouldBe false
            }

            test("configFile can be set and retrieved") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<KarakumTask>("gen").get()
                val cfg = project.layout.projectDirectory.file("karakum.config.json")

                task.configFile.set(cfg)

                task.configFile.get() shouldBe cfg
            }
        }
    }
}
