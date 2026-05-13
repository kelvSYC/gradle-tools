package com.kelvsyc.gradle.google.cloud.storage

import com.google.cloud.storage.Storage

/**
 * Test-only [StorageClientBuildService] that returns a pre-supplied mock [Storage] client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockStorageClientBuildService : StorageClientBuildService() {
    override fun createClient(): Storage = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: Storage? = null
    }
}
