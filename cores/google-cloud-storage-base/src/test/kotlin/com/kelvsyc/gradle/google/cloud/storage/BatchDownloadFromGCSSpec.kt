package com.kelvsyc.gradle.google.cloud.storage

import com.google.cloud.storage.Blob
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.Storage
import com.google.cloud.storage.StorageBatch
import com.google.cloud.storage.StorageException
import com.google.cloud.storage.TestStorageBatchResult
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.GradleException
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Path

class BatchDownloadFromGCSSpec : FunSpec() {
    init {
        context("AbstractBatchDownloadFromGCS - run()") {
            test("happy path - all blobs downloaded successfully") {
                val project = ProjectBuilder.builder().build()
                val outputFile = project.layout.buildDirectory.file("out/foo").get()

                val blob = mockk<Blob>(relaxed = true)
                val batchResult = TestStorageBatchResult()
                val storageBatch = mockk<StorageBatch>(relaxed = true)
                val storage = mockk<Storage>()

                every { storage.batch() } returns storageBatch
                every { storageBatch.get(any<BlobId>()) } returns batchResult

                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()
                task.client.set(storage)
                task.registerArtifact("foo") {
                    bucket.set("my-bucket")
                    blobName.set("path/to/blob")
                    this.outputFile.set(outputFile)
                }

                // Simulate the batch submit by triggering the callback synchronously
                every { storageBatch.submit() } answers {
                    batchResult.success(blob)
                }

                task.run()

                verify { blob.downloadTo(any<Path>()) }
            }

            test("blob not found - success(null) causes task failure") {
                val project = ProjectBuilder.builder().build()
                val outputFile = project.layout.buildDirectory.file("out/foo").get()

                val batchResult = TestStorageBatchResult()
                val storageBatch = mockk<StorageBatch>(relaxed = true)
                val storage = mockk<Storage>()

                every { storage.batch() } returns storageBatch
                every { storageBatch.get(any<BlobId>()) } returns batchResult
                every { storageBatch.submit() } answers { batchResult.success(null) }

                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()
                task.client.set(storage)
                task.registerArtifact("foo") {
                    bucket.set("my-bucket")
                    blobName.set("path/to/blob")
                    this.outputFile.set(outputFile)
                }

                shouldThrow<GradleException> { task.run() }
            }

            test("GCS error - error callback causes task failure") {
                val project = ProjectBuilder.builder().build()
                val outputFile = project.layout.buildDirectory.file("out/foo").get()

                val batchResult = TestStorageBatchResult()
                val storageBatch = mockk<StorageBatch>(relaxed = true)
                val storage = mockk<Storage>()

                every { storage.batch() } returns storageBatch
                every { storageBatch.get(any<BlobId>()) } returns batchResult
                every { storageBatch.submit() } answers {
                    batchResult.error(StorageException(500, "Internal Server Error"))
                }

                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()
                task.client.set(storage)
                task.registerArtifact("foo") {
                    bucket.set("my-bucket")
                    blobName.set("path/to/blob")
                    this.outputFile.set(outputFile)
                }

                shouldThrow<GradleException> { task.run() }
            }

            test("partial failure - one success and one error causes task failure") {
                val project = ProjectBuilder.builder().build()

                val batchResultFoo = TestStorageBatchResult()
                val batchResultBar = TestStorageBatchResult()
                val blob = mockk<Blob>(relaxed = true)
                val storageBatch = mockk<StorageBatch>(relaxed = true)
                val storage = mockk<Storage>()

                val fooBlobId = BlobId.of("bucket-a", "blob-a")
                val barBlobId = BlobId.of("bucket-b", "blob-b")

                every { storage.batch() } returns storageBatch
                every { storageBatch.get(fooBlobId) } returns batchResultFoo
                every { storageBatch.get(barBlobId) } returns batchResultBar
                every { storageBatch.submit() } answers {
                    batchResultFoo.success(blob)
                    batchResultBar.error(StorageException(404, "Not Found"))
                }

                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()
                task.client.set(storage)
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

                shouldThrow<GradleException> { task.run() }
            }

            test("all artifacts succeed - no exception thrown") {
                val project = ProjectBuilder.builder().build()

                val batchResultFoo = TestStorageBatchResult()
                val batchResultBar = TestStorageBatchResult()
                val blob = mockk<Blob>(relaxed = true)
                val storageBatch = mockk<StorageBatch>(relaxed = true)
                val storage = mockk<Storage>()

                val fooBlobId = BlobId.of("bucket-a", "blob-a")
                val barBlobId = BlobId.of("bucket-b", "blob-b")

                every { storage.batch() } returns storageBatch
                every { storageBatch.get(fooBlobId) } returns batchResultFoo
                every { storageBatch.get(barBlobId) } returns batchResultBar
                every { storageBatch.submit() } answers {
                    batchResultFoo.success(blob)
                    batchResultBar.success(blob)
                }

                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()
                task.client.set(storage)
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

                task.run() // should not throw
            }

            test("timeout - batch submit never fires callbacks causes GradleException") {
                val project = ProjectBuilder.builder().build()
                val outputFile = project.layout.buildDirectory.file("out/foo").get()

                val batchResult = TestStorageBatchResult()
                val storageBatch = mockk<StorageBatch>(relaxed = true)
                val storage = mockk<Storage>()

                every { storage.batch() } returns storageBatch
                every { storageBatch.get(any<BlobId>()) } returns batchResult
                // submit does NOT fire any callback — simulates a timeout scenario
                every { storageBatch.submit() } answers { /* no-op */ }

                val task = project.tasks.register<BatchDownloadFromGCS>("download").get()
                task.client.set(storage)
                task.registerArtifact("foo") {
                    bucket.set("my-bucket")
                    blobName.set("path/to/blob")
                    this.outputFile.set(outputFile)
                }

                val ex = shouldThrow<GradleException> { task.run() }
                ex.message shouldContain "Timed out"
            }
        }
    }
}
