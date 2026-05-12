package com.kelvsyc.gradle.azure.storage.blob

import com.azure.storage.blob.BlobServiceClient

/**
 * Test-only [BlobServiceClientBuildService] that returns a pre-supplied mock [BlobServiceClient].
 */
abstract class MockBlobServiceClientBuildService : BlobServiceClientBuildService() {
    override fun createClient(): BlobServiceClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: BlobServiceClient? = null
    }
}
