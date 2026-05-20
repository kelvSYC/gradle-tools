package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.JobsClient

/**
 * Test-only [CloudRunJobsClientBuildService] that returns a pre-supplied mock client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockCloudRunJobsClientBuildService : CloudRunJobsClientBuildService() {
    override fun createClient(): JobsClient =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: JobsClient? = null
    }
}
