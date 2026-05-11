package com.kelvsyc.gradle.internal.azure.storage.blob

import com.azure.storage.blob.BlobServiceClient
import com.kelvsyc.gradle.azure.storage.blob.MockBlobServiceClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockBlobServiceClientInfoInternal :
    MockBlobServiceClientInfo, ServiceClientInfoInternal<BlobServiceClient> {
    override fun createClient(): BlobServiceClient = mockk()
}
