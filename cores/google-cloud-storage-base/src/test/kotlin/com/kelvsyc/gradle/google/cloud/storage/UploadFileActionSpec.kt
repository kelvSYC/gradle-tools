package com.kelvsyc.gradle.google.cloud.storage

import com.google.cloud.WriteChannel
import com.google.cloud.storage.BlobId
import com.google.cloud.storage.BlobInfo
import com.google.cloud.storage.Storage
import com.kelvsyc.gradle.clients.ClientsBaseService
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.testfixtures.ProjectBuilder
import java.nio.ByteBuffer
import java.nio.file.Files
import kotlin.reflect.KClass

class UploadFileActionSpec : FunSpec() {
    init {
        context("UploadFileAction - execute()") {
            test("BlobId is constructed from bucket and blobName") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("upload-test", ".txt").also {
                    it.toFile().writeText("hello")
                }

                val writer = mockk<WriteChannel>(relaxed = true)
                val storage = mockk<Storage>(relaxed = true)
                val service = mockk<ClientsBaseService>(relaxed = true)

                every { service.getClient(any(), any<KClass<StorageClientInfo>>(), any<KClass<Storage>>()) } returns storage

                val blobInfoSlot = slot<BlobInfo>()
                every { storage.writer(capture(blobInfoSlot)) } returns writer

                val params = project.objects.newInstance<UploadFileAction.Parameters>()
                params.service.set(service)
                params.clientName.set("my-client")
                params.bucket.set("my-bucket")
                params.blobName.set("path/to/blob")
                params.inputFile.set(tempFile.toFile())

                val action = object : UploadFileAction() {
                    override fun getParameters() = params
                }
                action.execute()

                blobInfoSlot.captured.blobId shouldBe BlobId.of("my-bucket", "path/to/blob")

                tempFile.toFile().delete()
            }

            test("file contents are written to the GCS writer") {
                val project = ProjectBuilder.builder().build()
                val content = "hello world"
                val tempFile = Files.createTempFile("upload-test", ".txt").also {
                    it.toFile().writeText(content)
                }

                val writtenBuffers = mutableListOf<ByteBuffer>()
                val writer = mockk<WriteChannel>(relaxed = true)
                val storage = mockk<Storage>(relaxed = true)
                val service = mockk<ClientsBaseService>(relaxed = true)

                every { service.getClient(any(), any<KClass<StorageClientInfo>>(), any<KClass<Storage>>()) } returns storage
                every { storage.writer(any<BlobInfo>()) } returns writer
                every { writer.write(capture(writtenBuffers)) } answers {
                    writtenBuffers.last().remaining()
                }

                val params = project.objects.newInstance<UploadFileAction.Parameters>()
                params.service.set(service)
                params.clientName.set("my-client")
                params.bucket.set("my-bucket")
                params.blobName.set("path/to/blob")
                params.inputFile.set(tempFile.toFile())

                val action = object : UploadFileAction() {
                    override fun getParameters() = params
                }
                action.execute()

                val written = writtenBuffers.map { buf ->
                    val bytes = ByteArray(buf.limit())
                    buf.rewind()
                    buf.get(bytes)
                    bytes
                }.reduce { a, b -> a + b }

                written shouldBe content.toByteArray()

                tempFile.toFile().delete()
            }

            test("writer is closed after upload") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("upload-test", ".txt").also {
                    it.toFile().writeText("data")
                }

                val writer = mockk<WriteChannel>(relaxed = true)
                val storage = mockk<Storage>(relaxed = true)
                val service = mockk<ClientsBaseService>(relaxed = true)

                every { service.getClient(any(), any<KClass<StorageClientInfo>>(), any<KClass<Storage>>()) } returns storage
                every { storage.writer(any<BlobInfo>()) } returns writer

                val params = project.objects.newInstance<UploadFileAction.Parameters>()
                params.service.set(service)
                params.clientName.set("my-client")
                params.bucket.set("my-bucket")
                params.blobName.set("path/to/blob")
                params.inputFile.set(tempFile.toFile())

                val action = object : UploadFileAction() {
                    override fun getParameters() = params
                }
                action.execute()

                verify { writer.close() }

                tempFile.toFile().delete()
            }

            test("empty file - writer is opened and closed without writing any bytes") {
                val project = ProjectBuilder.builder().build()
                val tempFile = Files.createTempFile("upload-test", ".txt") // empty

                val writer = mockk<WriteChannel>(relaxed = true)
                val storage = mockk<Storage>(relaxed = true)
                val service = mockk<ClientsBaseService>(relaxed = true)

                every { service.getClient(any(), any<KClass<StorageClientInfo>>(), any<KClass<Storage>>()) } returns storage
                every { storage.writer(any<BlobInfo>()) } returns writer

                val params = project.objects.newInstance<UploadFileAction.Parameters>()
                params.service.set(service)
                params.clientName.set("my-client")
                params.bucket.set("my-bucket")
                params.blobName.set("path/to/blob")
                params.inputFile.set(tempFile.toFile())

                val action = object : UploadFileAction() {
                    override fun getParameters() = params
                }
                action.execute()

                verify(exactly = 0) { writer.write(any()) }
                verify { writer.close() }

                tempFile.toFile().delete()
            }

            test("large file - all bytes are written across multiple chunks") {
                val project = ProjectBuilder.builder().build()
                val chunkSize = 1024 * 1024
                val fileSize = chunkSize * 3 + 512 // 3 full chunks + partial
                val content = ByteArray(fileSize) { it.toByte() }
                val tempFile = Files.createTempFile("upload-test-large", ".bin").also {
                    it.toFile().writeBytes(content)
                }

                val writtenBuffers = mutableListOf<ByteBuffer>()
                val writer = mockk<WriteChannel>(relaxed = true)
                val storage = mockk<Storage>(relaxed = true)
                val service = mockk<ClientsBaseService>(relaxed = true)

                every { service.getClient(any(), any<KClass<StorageClientInfo>>(), any<KClass<Storage>>()) } returns storage
                every { storage.writer(any<BlobInfo>()) } returns writer
                every { writer.write(capture(writtenBuffers)) } answers {
                    writtenBuffers.last().remaining()
                }

                val params = project.objects.newInstance<UploadFileAction.Parameters>()
                params.service.set(service)
                params.clientName.set("my-client")
                params.bucket.set("my-bucket")
                params.blobName.set("path/to/blob")
                params.inputFile.set(tempFile.toFile())

                val action = object : UploadFileAction() {
                    override fun getParameters() = params
                }
                action.execute()

                val totalWritten = writtenBuffers.sumOf { it.limit() }
                totalWritten shouldBe fileSize

                tempFile.toFile().delete()
            }
        }
    }
}
