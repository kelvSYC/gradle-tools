package com.kelvsyc.gradle.bitbucket.server

/**
 * Test-only [BitbucketServerClientBuildService] that returns a pre-supplied mock [BitbucketServerService].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockBitbucketServerClientBuildService : BitbucketServerClientBuildService() {
    override fun createClient(): BitbucketServerService =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: BitbucketServerService? = null
    }
}
