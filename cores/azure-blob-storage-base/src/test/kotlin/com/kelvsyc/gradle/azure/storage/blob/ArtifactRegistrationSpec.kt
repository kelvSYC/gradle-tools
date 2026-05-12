package com.kelvsyc.gradle.azure.storage.blob

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class ArtifactRegistrationSpec : FunSpec() {
    init {
        context("BatchDownloadFromAzureBlobStorage - registerArtifact") {
            test("stores artifact under the given name") {
                val project = ProjectBuilder.builder().build()
                val task =
                    project.tasks.register<BatchDownloadFromAzureBlobStorage>("download").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.containerName.set("my-container")
                    artifact.blobName.set("path/to/blob")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts["foo"]!!.containerName.get() shouldBe "my-container"
                artifacts["foo"]!!.blobName.get() shouldBe "path/to/blob"
            }

            test("stores multiple artifacts under their respective names") {
                val project = ProjectBuilder.builder().build()
                val task =
                    project.tasks.register<BatchDownloadFromAzureBlobStorage>("download").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.containerName.set("container-a")
                    artifact.blobName.set("blob-a")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }
                task.registerArtifact("bar") { artifact ->
                    artifact.containerName.set("container-b")
                    artifact.blobName.set("blob-b")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/bar"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts shouldContainKey "bar"
                artifacts["foo"]!!.containerName.get() shouldBe "container-a"
                artifacts["bar"]!!.containerName.get() shouldBe "container-b"
            }

            test("registering a duplicate name overwrites the previous artifact") {
                val project = ProjectBuilder.builder().build()
                val task =
                    project.tasks.register<BatchDownloadFromAzureBlobStorage>("download").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.containerName.set("original-container")
                    artifact.blobName.set("original-blob")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }
                task.registerArtifact("foo") { artifact ->
                    artifact.containerName.set("new-container")
                    artifact.blobName.set("new-blob")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/foo2"))
                }

                val artifacts = task.artifacts.get()
                artifacts["foo"]!!.containerName.get() shouldBe "new-container"
            }

            test("outputFiles is empty when no artifacts are registered") {
                val project = ProjectBuilder.builder().build()
                val task =
                    project.tasks.register<BatchDownloadFromAzureBlobStorage>("download").get()

                task.outputFiles.get() shouldBe emptyMap()
            }

            test("outputFiles maps each artifact name to its outputFile") {
                val project = ProjectBuilder.builder().build()
                val task =
                    project.tasks.register<BatchDownloadFromAzureBlobStorage>("download").get()
                val expectedFile = project.layout.buildDirectory.file("out/foo").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.containerName.set("my-container")
                    artifact.blobName.set("path/to/blob")
                    artifact.outputFile.set(expectedFile)
                }

                val outputFiles = task.outputFiles.get()
                outputFiles shouldContainKey "foo"
                outputFiles["foo"]!!.asFile shouldBe expectedFile.asFile
            }
        }

        context("BatchUploadToAzureBlobStorage - registerArtifact") {
            test("stores artifact under the given name") {
                val project = ProjectBuilder.builder().build()
                val task =
                    project.tasks.register<BatchUploadToAzureBlobStorage>("upload").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.containerName.set("my-container")
                    artifact.blobName.set("path/to/blob")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts["foo"]!!.containerName.get() shouldBe "my-container"
                artifacts["foo"]!!.blobName.get() shouldBe "path/to/blob"
            }

            test("stores multiple artifacts under their respective names") {
                val project = ProjectBuilder.builder().build()
                val task =
                    project.tasks.register<BatchUploadToAzureBlobStorage>("upload").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.containerName.set("container-a")
                    artifact.blobName.set("blob-a")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }
                task.registerArtifact("bar") { artifact ->
                    artifact.containerName.set("container-b")
                    artifact.blobName.set("blob-b")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/bar.txt"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts shouldContainKey "bar"
            }

            test("registering a duplicate name overwrites the previous artifact") {
                val project = ProjectBuilder.builder().build()
                val task =
                    project.tasks.register<BatchUploadToAzureBlobStorage>("upload").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.containerName.set("original-container")
                    artifact.blobName.set("original-blob")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }
                task.registerArtifact("foo") { artifact ->
                    artifact.containerName.set("new-container")
                    artifact.blobName.set("new-blob")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/foo2.txt"))
                }

                val artifacts = task.artifacts.get()
                artifacts["foo"]!!.containerName.get() shouldBe "new-container"
            }

            test("artifacts is empty when no artifacts are registered") {
                val project = ProjectBuilder.builder().build()
                val task =
                    project.tasks.register<BatchUploadToAzureBlobStorage>("upload").get()

                task.artifacts.get() shouldBe emptyMap()
            }

            test("artifact does not contain unregistered names") {
                val project = ProjectBuilder.builder().build()
                val task =
                    project.tasks.register<BatchUploadToAzureBlobStorage>("upload").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.containerName.set("my-container")
                    artifact.blobName.set("blob")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }

                task.artifacts.get() shouldNotContainKey "bar"
            }
        }
    }
}
