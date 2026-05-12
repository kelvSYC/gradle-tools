package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.nio.file.Files

class DownloadBlobActionSpec : FunSpec() {
    init {
        test("execute - navigates client hierarchy and downloads to file") {
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
            every { mockBlobClient.downloadToFile(any<String>(), any<Boolean>()) } returns mockk()

            val outputFile = Files.createTempFile("download-test", ".bin")

            val params = project.objects.newInstance<DownloadBlobAction.Parameters>()
            params.service.set(service)
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
