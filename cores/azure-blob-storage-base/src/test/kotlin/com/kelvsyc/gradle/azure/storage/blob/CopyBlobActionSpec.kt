package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.azure.storage.blob.MockBlobServiceClientInfoInternal
import com.kelvsyc.gradle.plugins.AzureBlobStorageBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class CopyBlobActionSpec : FunSpec() {
    init {
        test("execute - copies from source to destination via copyFromUrl") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(AzureBlobStorageBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get()
                .registerBinding(MockBlobServiceClientInfo::class, MockBlobServiceClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockBlobServiceClientInfo>("mock") {}

            val client = extension.getClient<BlobServiceClient, MockBlobServiceClientInfo>("mock").get()
            val srcContainerClient = mockk<BlobContainerClient>()
            val dstContainerClient = mockk<BlobContainerClient>()
            val srcBlobClient = mockk<BlobClient>()
            val dstBlobClient = mockk<BlobClient>()

            every { client.getBlobContainerClient("src-container") } returns srcContainerClient
            every { client.getBlobContainerClient("dst-container") } returns dstContainerClient
            every { srcContainerClient.getBlobClient("src-blob") } returns srcBlobClient
            every { dstContainerClient.getBlobClient("dst-blob") } returns dstBlobClient
            every { srcBlobClient.blobUrl } returns "https://account.blob.core.windows.net/src-container/src-blob"

            val urlSlot = slot<String>()
            every { dstBlobClient.copyFromUrl(capture(urlSlot)) } returns "copy-id"

            val params = project.objects.newInstance<CopyBlobAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.sourceContainerName.set("src-container")
            params.sourceBlobName.set("src-blob")
            params.destinationContainerName.set("dst-container")
            params.destinationBlobName.set("dst-blob")

            val action = object : CopyBlobAction() {
                override fun getParameters() = params
            }
            action.execute()

            urlSlot.captured shouldBe "https://account.blob.core.windows.net/src-container/src-blob"
        }
    }
}
