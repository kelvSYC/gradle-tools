package com.kelvsyc.gradle.azure.storage.blob

import com.azure.core.util.BinaryData
import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.azure.storage.blob.MockBlobServiceClientInfoInternal
import com.kelvsyc.gradle.plugins.AzureBlobStorageBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.equals.shouldBeEqual
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class AbstractBlobStorageValueSourceSpec : FunSpec() {
    abstract class StringValueSource :
        AbstractBlobStorageValueSource<String, AbstractBlobStorageValueSource.Parameters>() {
        override fun doObtain(content: BinaryData): String = content.toString()
    }

    init {
        test("get - navigates client hierarchy and returns transformed content") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(AzureBlobStorageBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get()
                .registerBinding(MockBlobServiceClientInfo::class, MockBlobServiceClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockBlobServiceClientInfo>("mock") {}

            val client = extension.getClient<BlobServiceClient, MockBlobServiceClientInfo>("mock").get()
            val mockContainerClient = mockk<BlobContainerClient>()
            val mockBlobClient = mockk<BlobClient>()
            every { client.getBlobContainerClient("my-container") } returns mockContainerClient
            every { mockContainerClient.getBlobClient("my-blob") } returns mockBlobClient
            every { mockBlobClient.downloadContent() } returns BinaryData.fromString("hello world")

            val provider = project.providers.of(StringValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.containerName.set("my-container")
                parameters.blobName.set("my-blob")
            }
            val result = provider.get()

            result shouldBeEqual "hello world"
        }
    }
}
