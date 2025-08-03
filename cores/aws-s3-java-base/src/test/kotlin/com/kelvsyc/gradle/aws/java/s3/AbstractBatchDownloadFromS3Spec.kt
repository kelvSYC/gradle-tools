package com.kelvsyc.gradle.aws.java.s3

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.DownloadFileRequest
import software.amazon.awssdk.transfer.s3.model.FileDownload
import java.util.concurrent.CompletableFuture
import kotlin.io.path.name

class AbstractBatchDownloadFromS3Spec : FunSpec() {
    init {
        test("Download single") {
            val transferManager = mockk<S3TransferManager>()
            val download = mockk<FileDownload>()
            val slot = slot<DownloadFileRequest>()
            every { transferManager.downloadFile(capture(slot))} returns download
            every { download.completionFuture() } returns CompletableFuture.completedFuture(mockk())
            val project = ProjectBuilder.builder().build()

            val task = project.tasks.register<AbstractBatchDownloadFromS3>("myTask") {
                client.set(transferManager)
                registerArtifact("artifact1") {
                    bucket.set("bucket")
                    key.set("key")
                    outputFile.set(project.layout.buildDirectory.file("filename"))
                }
            }
            // Simulate running the task
            task.get().run()

            val innerRequest = slot.captured
            innerRequest.objectRequest.bucket() shouldBeEqual "bucket"
            innerRequest.objectRequest.key() shouldBeEqual "key"
            innerRequest.destination().name shouldBeEqual "filename"
        }

        test("Download multiple") {
            val transferManager = mockk<S3TransferManager>()
            val download = mockk<FileDownload>()
            val slot = mutableListOf<DownloadFileRequest>()
            every { transferManager.downloadFile(capture(slot))} returns download
            every { download.completionFuture() } returns CompletableFuture.completedFuture(mockk())
            val project = ProjectBuilder.builder().build()

            val task = project.tasks.register<AbstractBatchDownloadFromS3>("myTask") {
                client.set(transferManager)
                registerArtifact("artifact1") {
                    bucket.set("bucket1")
                    key.set("key1")
                    outputFile.set(project.layout.buildDirectory.file("filename1"))
                }
                registerArtifact("artifact2") {
                    bucket.set("bucket2")
                    key.set("key2")
                    outputFile.set(project.layout.buildDirectory.file("filename2"))
                }
            }
            // Simulate running the task
            task.get().run()

            val artifacts = task.get().artifacts.get()
            slot.shouldHaveSize(artifacts.size)
            task.get().artifacts.get().values.forEach { artifact ->
                slot.any { it.objectRequest.bucket() == artifact.bucket.get() && it.objectRequest.key() == artifact.key.get() }.shouldBeTrue()
            }
        }
    }
}
