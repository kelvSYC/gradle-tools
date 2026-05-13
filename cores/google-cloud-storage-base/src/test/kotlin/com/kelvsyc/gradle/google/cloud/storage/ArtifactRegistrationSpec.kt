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

                task.registerArtifact("foo") { artifact ->
                    artifact.bucket.set("my-bucket")
                    artifact.blobName.set("path/to/blob")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts["foo"]!!.bucket.get() shouldBe "my-bucket"
                artifacts["foo"]!!.blobName.get() shouldBe "path/to/blob"
            }

            test("stores multiple artifacts under their respective names") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.bucket.set("bucket-a")
                    artifact.blobName.set("blob-a")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }
                task.registerArtifact("bar") { artifact ->
                    artifact.bucket.set("bucket-b")
                    artifact.blobName.set("blob-b")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/bar"))
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

                task.registerArtifact("foo") { artifact ->
                    artifact.bucket.set("original-bucket")
                    artifact.blobName.set("original-blob")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }
                task.registerArtifact("foo") { artifact ->
                    artifact.bucket.set("new-bucket")
                    artifact.blobName.set("new-blob")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/foo2"))
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

                task.registerArtifact("foo") { artifact ->
                    artifact.bucket.set("my-bucket")
                    artifact.blobName.set("path/to/blob")
                    artifact.outputFile.set(expectedFile)
                }

                val outputFiles = task.outputFiles.get()
                outputFiles shouldContainKey "foo"
                outputFiles["foo"]!!.asFile shouldBe expectedFile.asFile
            }

            test("outputFiles contains entries for all registered artifacts") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.bucket.set("bucket-a")
                    artifact.blobName.set("blob-a")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/foo"))
                }
                task.registerArtifact("bar") { artifact ->
                    artifact.bucket.set("bucket-b")
                    artifact.blobName.set("blob-b")
                    artifact.outputFile.set(project.layout.buildDirectory.file("out/bar"))
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

                task.registerArtifact("foo") { artifact ->
                    artifact.bucket.set("my-bucket")
                    artifact.blobName.set("path/to/blob")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts["foo"]!!.bucket.get() shouldBe "my-bucket"
                artifacts["foo"]!!.blobName.get() shouldBe "path/to/blob"
            }

            test("stores multiple artifacts under their respective names") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchUploadToGCS>("upload").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.bucket.set("bucket-a")
                    artifact.blobName.set("blob-a")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }
                task.registerArtifact("bar") { artifact ->
                    artifact.bucket.set("bucket-b")
                    artifact.blobName.set("blob-b")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/bar.txt"))
                }

                val artifacts = task.artifacts.get()
                artifacts shouldContainKey "foo"
                artifacts shouldContainKey "bar"
            }

            test("registering a duplicate name overwrites the previous artifact") {
                val project = ProjectBuilder.builder().build()
                val task = project.tasks.register<BatchUploadToGCS>("upload").get()

                task.registerArtifact("foo") { artifact ->
                    artifact.bucket.set("original-bucket")
                    artifact.blobName.set("original-blob")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }
                task.registerArtifact("foo") { artifact ->
                    artifact.bucket.set("new-bucket")
                    artifact.blobName.set("new-blob")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/foo2.txt"))
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

                task.registerArtifact("foo") { artifact ->
                    artifact.bucket.set("my-bucket")
                    artifact.blobName.set("blob")
                    artifact.inputFile.set(project.layout.projectDirectory.file("src/foo.txt"))
                }

                task.artifacts.get() shouldNotContainKey "bar"
            }
        }
    }
}
