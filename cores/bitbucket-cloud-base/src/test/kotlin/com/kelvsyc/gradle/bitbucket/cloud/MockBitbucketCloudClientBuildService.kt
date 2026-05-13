package com.kelvsyc.gradle.bitbucket.cloud

/**
 * Test-only [BitbucketCloudClientBuildService] that returns a pre-supplied mock [BitbucketCloudService].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockBitbucketCloudClientBuildService : BitbucketCloudClientBuildService() {
    override fun createClient(): BitbucketCloudService =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: BitbucketCloudService? = null
    }
}
