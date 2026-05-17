package com.kelvsyc.gradle.gitea

/**
 * Test-only [GiteaBasicClientBuildService] that returns a pre-supplied mock [GiteaService].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockGiteaBasicClientBuildService : GiteaBasicClientBuildService() {
    override fun createClient(): GiteaService = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: GiteaService? = null
    }
}

