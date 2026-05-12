package com.kelvsyc.gradle.aws.java.sts

import software.amazon.awssdk.services.sts.StsClient

/**
 * Test-only [StsClientBuildService] that returns a pre-supplied mock [StsClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockStsClientBuildService : StsClientBuildService() {
    override fun createClient(): StsClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: StsClient? = null
    }
}
