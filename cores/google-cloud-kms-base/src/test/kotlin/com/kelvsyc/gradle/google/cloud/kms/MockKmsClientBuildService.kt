package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.KeyManagementServiceClient

/**
 * Test-only [KmsClientBuildService] that returns a pre-supplied mock client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockKmsClientBuildService : KmsClientBuildService() {
    override fun createClient(): KeyManagementServiceClient =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: KeyManagementServiceClient? = null
    }
}
