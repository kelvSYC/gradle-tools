package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.ListBlobsOptions
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AbstractListBlobsValueSourceSpec : FunSpec() {
    abstract class BlobNamesValueSource :
        AbstractListBlobsValueSource<List<String>, AbstractListBlobsValueSource.Parameters>() {
        override fun doObtain(blobs: List<BlobItem>): List<String> = blobs.map { it.name }
    }

    init {
        test("get - lists blobs and forwards prefix") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<BlobServiceClient>()
            MockBlobServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "blob-service",
                MockBlobServiceClientBuildService::class
            )
            val mockContainerClient = mockk<BlobContainerClient>()
            every { client.getBlobContainerClient("my-container") } returns mockContainerClient

            val item1 = mockk<BlobItem>()
            val item2 = mockk<BlobItem>()
            every { item1.name } returns "artifacts/a.txt"
            every { item2.name } returns "artifacts/b.txt"

            val optionsSlot = slot<ListBlobsOptions>()
            val mockIterable = mockk<com.azure.core.http.rest.PagedIterable<BlobItem>>()
            every { mockIterable.iterator() } returns mutableListOf(item1, item2).iterator()
            every { mockContainerClient.listBlobs(capture(optionsSlot), any()) } returns mockIterable

            val provider = project.providers.ofKt(BlobNamesValueSource::class) {
                parameters.service.set(service)
                parameters.containerName.set("my-container")
                parameters.prefix.set("artifacts/")
            }
            val result = provider.get()

            result shouldContainExactly listOf("artifacts/a.txt", "artifacts/b.txt")
            optionsSlot.captured.prefix shouldBe "artifacts/"
        }

        test("get - omits prefix when unset") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<BlobServiceClient>()
            MockBlobServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "blob-service",
                MockBlobServiceClientBuildService::class
            )
            val mockContainerClient = mockk<BlobContainerClient>()
            every { client.getBlobContainerClient("my-container") } returns mockContainerClient

            val optionsSlot = slot<ListBlobsOptions>()
            val mockIterable = mockk<com.azure.core.http.rest.PagedIterable<BlobItem>>()
            every { mockIterable.iterator() } returns mutableListOf<BlobItem>().iterator()
            every { mockContainerClient.listBlobs(capture(optionsSlot), any()) } returns mockIterable

            val provider = project.providers.ofKt(BlobNamesValueSource::class) {
                parameters.service.set(service)
                parameters.containerName.set("my-container")
            }
            provider.get()

            optionsSlot.captured.prefix.shouldBeNull()
        }
    }
}
