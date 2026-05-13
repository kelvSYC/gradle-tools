package com.kelvsyc.gradle.aws.java.ecr

import software.amazon.awssdk.services.ecr.EcrClient

/**
 * Test-only [EcrClientBuildService] that returns a pre-supplied mock [EcrClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockEcrClientBuildService : EcrClientBuildService() {
    override fun createClient(): EcrClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: EcrClient? = null
    }
}
