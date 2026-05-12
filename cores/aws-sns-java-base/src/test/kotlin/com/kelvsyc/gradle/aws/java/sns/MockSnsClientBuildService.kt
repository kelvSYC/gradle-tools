package com.kelvsyc.gradle.aws.java.sns

import software.amazon.awssdk.services.sns.SnsClient

/**
 * Test-only [SnsClientBuildService] that returns a pre-supplied mock [SnsClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockSnsClientBuildService : SnsClientBuildService() {
    override fun createClient(): SnsClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: SnsClient? = null
    }
}
