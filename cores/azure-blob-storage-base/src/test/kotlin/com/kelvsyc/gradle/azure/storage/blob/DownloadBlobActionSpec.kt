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
import io.mockk.verify
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class DownloadBlobActionSpec : FunSpec() {
    init {
        test("execute - navigates client hierarchy and downloads to file") {
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
            every { mockBlobClient.downloadToFile(any<String>(), any<Boolean>()) } returns mockk()

            val outputFile = Files.createTempFile("download-test", ".bin")

            val params = project.objects.newInstance<DownloadBlobAction.Parameters>()
            params.service.set(extension.service.get())
            params.clientName.set("mock")
            params.containerName.set("my-container")
            params.blobName.set("my-blob")
            params.outputFile.set(outputFile.toFile())

            val action = object : DownloadBlobAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { client.getBlobContainerClient("my-container") }
            verify { mockContainerClient.getBlobClient("my-blob") }
            verify { mockBlobClient.downloadToFile(outputFile.toFile().absolutePath, true) }

            outputFile.toFile().delete()
        }
    }
}
