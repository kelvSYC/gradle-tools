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

                task.registerArtifact("foo") {
                    containerName.set("my-container")
                    blobName.set("path/to/blob")
                    outputFile.set(project.layout.buildDirectory.file("out/foo"))
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

                task.registerArtifact("foo") {
                    containerName.set("container-a")
                    blobName.set("blob-a")
                    outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }
                task.registerArtifact("bar") {
                    containerName.set("container-b")
                    blobName.set("blob-b")
                    outputFile.set(project.layout.buildDirectory.file("out/bar"))
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

                task.registerArtifact("foo") {
                    containerName.set("original-container")
                    blobName.set("original-blob")
                    outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }
                task.registerArtifact("foo") {
                    containerName.set("new-container")
                    blobName.set("new-blob")
                    outputFile.set(project.layout.buildDirectory.file("out/foo2"))
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

                task.registerArtifact("foo") {
                    containerName.set("my-container")
                    blobName.set("path/to/blob")
                    outputFile.set(expectedFile)
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

                task.registerArtifact("foo") {
                    containerName.set("my-container")
                    blobName.set("path/to/blob")
                    inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
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

                task.registerArtifact("foo") {
                    containerName.set("container-a")
                    blobName.set("blob-a")
                    inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }
                task.registerArtifact("bar") {
                    containerName.set("container-b")
                    blobName.set("blob-b")
                    inputFile.set(project.layout.projectDirectory.file("src/bar.txt"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts shouldContainKey "bar"
            }

            test("registering a duplicate name overwrites the previous artifact") {
                val project = ProjectBuilder.builder().build()
                val task =
                    project.tasks.register<BatchUploadToAzureBlobStorage>("upload").get()

                task.registerArtifact("foo") {
                    containerName.set("original-container")
                    blobName.set("original-blob")
                    inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }
                task.registerArtifact("foo") {
                    containerName.set("new-container")
                    blobName.set("new-blob")
                    inputFile.set(project.layout.projectDirectory.file("src/foo2.txt"))
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

                task.registerArtifact("foo") {
                    containerName.set("my-container")
                    blobName.set("blob")
                    inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }

                task.artifacts.get() shouldNotContainKey "bar"
            }
        }
    }
}
