package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobClient
import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.engine.spec.tempfile
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UploadBlobActionSpec : FunSpec() {
    init {
        test("execute - navigates client hierarchy and uploads from file") {
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
            every { mockBlobClient.uploadFromFile(any<String>(), any<Boolean>()) } returns mockk()

            val inputFile = tempfile()

            val params = project.objects.newInstance<UploadBlobAction.Parameters>()
            params.service.set(service)
            params.containerName.set("my-container")
            params.blobName.set("my-blob")
            params.inputFile.set(inputFile)

            val action = object : UploadBlobAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { client.getBlobContainerClient("my-container") }
            verify { mockContainerClient.getBlobClient("my-blob") }
            verify { mockBlobClient.uploadFromFile(inputFile.absolutePath, true) }
        }
    }
}
