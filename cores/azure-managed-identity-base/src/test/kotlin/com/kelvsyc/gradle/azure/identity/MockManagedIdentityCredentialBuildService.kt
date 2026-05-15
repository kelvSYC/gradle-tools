package com.kelvsyc.gradle.azure.identity

import com.azure.identity.ManagedIdentityCredential

/**
 * Test-only [ManagedIdentityCredentialBuildService] that returns a pre-supplied mock [ManagedIdentityCredential].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockManagedIdentityCredentialBuildService : ManagedIdentityCredentialBuildService() {
    override fun createClient(): ManagedIdentityCredential =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: ManagedIdentityCredential? = null
    }
}

