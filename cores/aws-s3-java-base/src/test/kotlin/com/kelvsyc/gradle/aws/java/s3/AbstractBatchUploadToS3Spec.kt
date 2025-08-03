package com.kelvsyc.gradle.aws.java.s3

import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.kotest.matchers.booleans.shouldBeTrue
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.register
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.transfer.s3.S3TransferManager
import software.amazon.awssdk.transfer.s3.model.FileUpload
import software.amazon.awssdk.transfer.s3.model.UploadFileRequest
import java.util.concurrent.CompletableFuture
import kotlin.io.path.name

class AbstractBatchUploadToS3Spec : FunSpec() {
    init {
        test("Upload single") {
            val transferManager = mockk<S3TransferManager>()
            val upload = mockk<FileUpload>()
            val slot = slot<UploadFileRequest>()
            every { transferManager.uploadFile(capture(slot))} returns upload
            every { upload.completionFuture() } returns CompletableFuture.completedFuture(mockk())
            val file = tempfile()

            val project = ProjectBuilder.builder().build()

            val task = project.tasks.register<AbstractBatchUploadToS3>("myTask") {
                client.set(transferManager)
                registerArtifact("artifact1") {
                    bucket.set("bucket")
                    key.set("key")
                    inputFile.fileValue(file)
                }
            }
            // Simulate running the task
            task.get().run()

            val innerRequest = slot.captured
            innerRequest.putObjectRequest().bucket() shouldBeEqual "bucket"
            innerRequest.putObjectRequest().key() shouldBeEqual "key"
            innerRequest.source().name shouldBeEqual file.name
        }

        test("Upload multiple") {
            val transferManager = mockk<S3TransferManager>()
            val upload = mockk<FileUpload>()
            val slot = mutableListOf<UploadFileRequest>()
            every { transferManager.uploadFile(capture(slot))} returns upload
            every { upload.completionFuture() } returns CompletableFuture.completedFuture(mockk())
            val file = tempfile()

            val project = ProjectBuilder.builder().build()

            val task = project.tasks.register<AbstractBatchUploadToS3>("myTask") {
                client.set(transferManager)
                registerArtifact("artifact1") {
                    bucket.set("bucket")
                    key.set("key")
                    inputFile.fileValue(file)
                }
            }
            // Simulate running the task
            task.get().run()

            val artifacts = task.get().artifacts.get()
            slot.shouldHaveSize(artifacts.size)
            task.get().artifacts.get().values.forEach { artifact ->
                slot.any { it.putObjectRequest().bucket() == artifact.bucket.get() && it.putObjectRequest().key() == artifact.key.get() }.shouldBeTrue()
            }
        }
    }
}
