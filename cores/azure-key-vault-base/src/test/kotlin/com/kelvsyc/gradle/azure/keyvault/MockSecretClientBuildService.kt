package com.kelvsyc.gradle.azure.keyvault

import com.azure.security.keyvault.secrets.SecretClient

/**
 * Test-only [SecretClientBuildService] that returns a pre-supplied mock [SecretClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockSecretClientBuildService : SecretClientBuildService() {
    override fun createClient(): SecretClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: SecretClient? = null
    }
}
