package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.ExecutionsClient

/**
 * Test-only [CloudRunExecutionsClientBuildService] that returns a pre-supplied mock client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockCloudRunExecutionsClientBuildService : CloudRunExecutionsClientBuildService() {
    override fun createClient(): ExecutionsClient =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: ExecutionsClient? = null
    }
}
