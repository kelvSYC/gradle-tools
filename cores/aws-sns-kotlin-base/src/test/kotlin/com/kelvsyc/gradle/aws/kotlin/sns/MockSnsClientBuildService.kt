package com.kelvsyc.gradle.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.SnsClient

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
