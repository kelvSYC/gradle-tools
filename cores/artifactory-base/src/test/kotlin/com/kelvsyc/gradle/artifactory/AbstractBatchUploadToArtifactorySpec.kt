package com.kelvsyc.gradle.artifactory

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.maps.shouldHaveSize
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class AbstractBatchUploadToArtifactorySpec : FunSpec() {
    init {
        test("registerArtifact - single artifact") {
            val project = ProjectBuilder.builder().build()
            val file = tempfile()

            val task = project.tasks.register<AbstractBatchUploadToArtifactory>("myTask") {
                registerArtifact("artifact1") { artifact ->
                    artifact.repository.set("my-repo")
                    artifact.path.set("my/path")
                    artifact.inputFile.set(file)
                }
            }

            val artifacts = task.get().artifacts.get()
            artifacts shouldHaveSize 1
            artifacts["artifact1"]!!.repository.get() shouldBe "my-repo"
            artifacts["artifact1"]!!.path.get() shouldBe "my/path"
        }

        test("registerArtifact - multiple artifacts") {
            val project = ProjectBuilder.builder().build()
            val file1 = tempfile()
            val file2 = tempfile()

            val task = project.tasks.register<AbstractBatchUploadToArtifactory>("myTask") {
                registerArtifact("first") { artifact ->
                    artifact.repository.set("repo-a")
                    artifact.path.set("path/a")
                    artifact.inputFile.set(file1)
                }
                registerArtifact("second") { artifact ->
                    artifact.repository.set("repo-b")
                    artifact.path.set("path/b")
                    artifact.inputFile.set(file2)
                }
            }

            val artifacts = task.get().artifacts.get()
            artifacts shouldHaveSize 2
            artifacts.keys shouldContainExactly setOf("first", "second")
        }

        test("inputFiles - maps artifact names to input file locations") {
            val project = ProjectBuilder.builder().build()
            val file = tempfile()

            val task = project.tasks.register<AbstractBatchUploadToArtifactory>("myTask") {
                registerArtifact("artifact1") { artifact ->
                    artifact.repository.set("my-repo")
                    artifact.path.set("my/path")
                    artifact.inputFile.set(file)
                }
            }

            val inputFiles = task.get().inputFiles.get()
            inputFiles shouldHaveSize 1
            inputFiles["artifact1"]!!.asFile.name shouldBe file.name
        }
    }
}
