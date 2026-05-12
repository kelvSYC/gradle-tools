package com.kelvsyc.gradle.artifactory

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class AbstractBatchDownloadFromArtifactorySpec : FunSpec() {
    init {
        test("registerArtifact - single artifact") {
            val project = ProjectBuilder.builder().build()

            val task = project.tasks.register<AbstractBatchDownloadFromArtifactory>("myTask") {
                registerArtifact("artifact1") {
                    repository.set("my-repo")
                    path.set("my/path")
                    outputFile.set(project.layout.buildDirectory.file("output.bin"))
                }
            }

            val artifacts = task.get().artifacts.get()
            artifacts shouldHaveSize 1
            artifacts["artifact1"]!!.repository.get() shouldBe "my-repo"
            artifacts["artifact1"]!!.path.get() shouldBe "my/path"
        }

        test("registerArtifact - multiple artifacts") {
            val project = ProjectBuilder.builder().build()

            val task = project.tasks.register<AbstractBatchDownloadFromArtifactory>("myTask") {
                registerArtifact("first") {
                    repository.set("repo-a")
                    path.set("path/a")
                    outputFile.set(project.layout.buildDirectory.file("a.bin"))
                }
                registerArtifact("second") {
                    repository.set("repo-b")
                    path.set("path/b")
                    outputFile.set(project.layout.buildDirectory.file("b.bin"))
                }
            }

            val artifacts = task.get().artifacts.get()
            artifacts shouldHaveSize 2
            artifacts.keys shouldContainExactly setOf("first", "second")
        }

        test("outputFiles - maps artifact names to output file locations") {
            val project = ProjectBuilder.builder().build()

            val task = project.tasks.register<AbstractBatchDownloadFromArtifactory>("myTask") {
                registerArtifact("artifact1") {
                    repository.set("my-repo")
                    path.set("my/path")
                    outputFile.set(project.layout.buildDirectory.file("output.bin"))
                }
            }

            val outputFiles = task.get().outputFiles.get()
            outputFiles shouldHaveSize 1
            outputFiles["artifact1"]!!.asFile.name shouldBe "output.bin"
        }
    }
}
