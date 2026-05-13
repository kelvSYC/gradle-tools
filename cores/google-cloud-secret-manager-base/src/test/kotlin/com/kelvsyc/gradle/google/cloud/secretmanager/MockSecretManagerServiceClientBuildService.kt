package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient

/**
 * Test-only [SecretManagerServiceClientBuildService] that returns a pre-supplied mock client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockSecretManagerServiceClientBuildService : SecretManagerServiceClientBuildService() {
    override fun createClient(): SecretManagerServiceClient =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: SecretManagerServiceClient? = null
    }
}
