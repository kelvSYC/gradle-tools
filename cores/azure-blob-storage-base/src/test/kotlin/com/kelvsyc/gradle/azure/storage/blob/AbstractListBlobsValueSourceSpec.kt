package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobContainerClient
import com.azure.storage.blob.BlobServiceClient
import com.azure.storage.blob.models.BlobItem
import com.azure.storage.blob.models.ListBlobsOptions
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.azure.storage.blob.MockBlobServiceClientInfoInternal
import com.kelvsyc.gradle.plugins.AzureBlobStorageBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.kotest.matchers.shouldBe
import io.kotest.matchers.nulls.shouldBeNull
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class AbstractListBlobsValueSourceSpec : FunSpec() {
    abstract class BlobNamesValueSource :
        AbstractListBlobsValueSource<List<String>, AbstractListBlobsValueSource.Parameters>() {
        override fun doObtain(blobs: List<BlobItem>): List<String> = blobs.map { it.name }
    }

    init {
        test("get - lists blobs and forwards prefix") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(AzureBlobStorageBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get()
                .registerBinding(MockBlobServiceClientInfo::class, MockBlobServiceClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockBlobServiceClientInfo>("mock") {}

            val client = extension.getClient<BlobServiceClient, MockBlobServiceClientInfo>("mock").get()
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

            val provider = project.providers.of(BlobNamesValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.containerName.set("my-container")
                parameters.prefix.set("artifacts/")
            }
            val result = provider.get()

            result shouldContainExactly listOf("artifacts/a.txt", "artifacts/b.txt")
            optionsSlot.captured.prefix shouldBe "artifacts/"
        }

        test("get - omits prefix when unset") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(AzureBlobStorageBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get()
                .registerBinding(MockBlobServiceClientInfo::class, MockBlobServiceClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockBlobServiceClientInfo>("mock") {}

            val client = extension.getClient<BlobServiceClient, MockBlobServiceClientInfo>("mock").get()
            val mockContainerClient = mockk<BlobContainerClient>()
            every { client.getBlobContainerClient("my-container") } returns mockContainerClient

            val optionsSlot = slot<ListBlobsOptions>()
            val mockIterable = mockk<com.azure.core.http.rest.PagedIterable<BlobItem>>()
            every { mockIterable.iterator() } returns mutableListOf<BlobItem>().iterator()
            every { mockContainerClient.listBlobs(capture(optionsSlot), any()) } returns mockIterable

            val provider = project.providers.of(BlobNamesValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.containerName.set("my-container")
            }
            provider.get()

            optionsSlot.captured.prefix.shouldBeNull()
        }
    }
}
