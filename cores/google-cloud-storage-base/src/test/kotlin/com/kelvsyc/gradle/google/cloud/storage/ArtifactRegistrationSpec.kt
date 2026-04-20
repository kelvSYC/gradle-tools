package com.kelvsyc.gradle.google.cloud.storage

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainKey
import io.kotest.matchers.maps.shouldNotContainKey
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder

class ArtifactRegistrationSpec : FunSpec() {
    init {
        context("BatchDownloadFromGCS - registerArtifact") {
            test("stores artifact under the given name") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()

                task.registerArtifact("foo") {
                    bucket.set("my-bucket")
                    blobName.set("path/to/blob")
                    outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts["foo"]!!.bucket.get() shouldBe "my-bucket"
                artifacts["foo"]!!.blobName.get() shouldBe "path/to/blob"
            }

            test("stores multiple artifacts under their respective names") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()

                task.registerArtifact("foo") {
                    bucket.set("bucket-a")
                    blobName.set("blob-a")
                    outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }
                task.registerArtifact("bar") {
                    bucket.set("bucket-b")
                    blobName.set("blob-b")
                    outputFile.set(project.layout.buildDirectory.file("out/bar"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts shouldContainKey "bar"
                artifacts["foo"]!!.bucket.get() shouldBe "bucket-a"
                artifacts["bar"]!!.bucket.get() shouldBe "bucket-b"
            }

            test("registering a duplicate name overwrites the previous artifact") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()

                task.registerArtifact("foo") {
                    bucket.set("original-bucket")
                    blobName.set("original-blob")
                    outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }
                task.registerArtifact("foo") {
                    bucket.set("new-bucket")
                    blobName.set("new-blob")
                    outputFile.set(project.layout.buildDirectory.file("out/foo2"))
                }

                val artifacts = task.artifacts.get()
                artifacts["foo"]!!.bucket.get() shouldBe "new-bucket"
            }

            test("outputFiles is empty when no artifacts are registered") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()

                task.outputFiles.get() shouldBe emptyMap()
            }

            test("outputFiles maps each artifact name to its outputFile") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()
                val expectedFile = project.layout.buildDirectory.file("out/foo").get()

                task.registerArtifact("foo") {
                    bucket.set("my-bucket")
                    blobName.set("path/to/blob")
                    outputFile.set(expectedFile)
                }

                val outputFiles = task.outputFiles.get()
                outputFiles shouldContainKey "foo"
                outputFiles["foo"]!!.asFile shouldBe expectedFile.asFile
            }

            test("outputFiles contains entries for all registered artifacts") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()

                task.registerArtifact("foo") {
                    bucket.set("bucket-a")
                    blobName.set("blob-a")
                    outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }
                task.registerArtifact("bar") {
                    bucket.set("bucket-b")
                    blobName.set("blob-b")
                    outputFile.set(project.layout.buildDirectory.file("out/bar"))
                }

                val outputFiles = task.outputFiles.get()
                outputFiles shouldContainKey "foo"
                outputFiles shouldContainKey "bar"
            }
        }

        context("BatchUploadToGCS - registerArtifact") {
            test("stores artifact under the given name") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchUploadToGCS>("upload").get()

                task.registerArtifact("foo") {
                    bucket.set("my-bucket")
                    blobName.set("path/to/blob")
                    inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts["foo"]!!.bucket.get() shouldBe "my-bucket"
                artifacts["foo"]!!.blobName.get() shouldBe "path/to/blob"
            }

            test("stores multiple artifacts under their respective names") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchUploadToGCS>("upload").get()

                task.registerArtifact("foo") {
                    bucket.set("bucket-a")
                    blobName.set("blob-a")
                    inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }
                task.registerArtifact("bar") {
                    bucket.set("bucket-b")
                    blobName.set("blob-b")
                    inputFile.set(project.layout.projectDirectory.file("src/bar.txt"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts shouldContainKey "bar"
            }

            test("registering a duplicate name overwrites the previous artifact") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchUploadToGCS>("upload").get()

                task.registerArtifact("foo") {
                    bucket.set("original-bucket")
                    blobName.set("original-blob")
                    inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }
                task.registerArtifact("foo") {
                    bucket.set("new-bucket")
                    blobName.set("new-blob")
                    inputFile.set(project.layout.projectDirectory.file("src/foo2.txt"))
                }

                val artifacts = task.artifacts.get()
                artifacts["foo"]!!.bucket.get() shouldBe "new-bucket"
            }

            test("artifacts is empty when no artifacts are registered") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchUploadToGCS>("upload").get()

                task.artifacts.get() shouldBe emptyMap()
            }

            test("artifact does not contain unregistered names") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchUploadToGCS>("upload").get()

                task.registerArtifact("foo") {
                    bucket.set("my-bucket")
                    blobName.set("blob")
                    inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }

                task.artifacts.get() shouldNotContainKey "bar"
            }
        }
    }
}
