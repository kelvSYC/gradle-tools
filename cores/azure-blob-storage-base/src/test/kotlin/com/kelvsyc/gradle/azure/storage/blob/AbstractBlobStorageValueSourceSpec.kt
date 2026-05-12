package com.kelvsyc.gradle.azure.storage.blob

import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AbstractBlobStorageValueSourceSpec : FunSpec() {
    abstract class StringValueSource :
        AbstractBlobStorageValueSource<String, AbstractBlobStorageValueSource.Parameters>() {
        override fun doObtain(content: BinaryData): String = content.toString()
    }

    init {
        test("get - navigates client hierarchy and returns transformed content") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<BlobServiceClient>()
            MockBlobServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "blob-service",
                MockBlobServiceClientBuildService::class
            )
            val mockContainerClient = mockk<BlobContainerClient>()
            val mockBlobClient = mockk<BlobClient>()
            every { client.getBlobContainerClient("my-container") } returns mockContainerClient
            every { mockContainerClient.getBlobClient("my-blob") } returns mockBlobClient
            every { mockBlobClient.downloadContent() } returns BinaryData.fromString("hello world")

            val provider = project.providers.ofKt(StringValueSource::class) {
                parameters.service.set(service)
                parameters.containerName.set("my-container")
                parameters.blobName.set("my-blob")
            }
            val result = provider.get()

            result shouldBeEqual "hello world"
        }
    }
}
