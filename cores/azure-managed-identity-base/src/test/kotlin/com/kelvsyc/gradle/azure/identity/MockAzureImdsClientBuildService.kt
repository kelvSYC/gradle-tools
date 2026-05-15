package com.kelvsyc.gradle.azure.identity

/**
 * Test-only [AzureImdsClientBuildService] that returns a pre-supplied mock [AzureImdsService].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockAzureImdsClientBuildService : AzureImdsClientBuildService() {
    override fun createClient(): AzureImdsService =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: AzureImdsService? = null
    }
}

