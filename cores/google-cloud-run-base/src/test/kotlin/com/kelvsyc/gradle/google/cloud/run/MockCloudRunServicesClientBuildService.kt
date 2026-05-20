package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.ServicesClient

/**
 * Test-only [CloudRunServicesClientBuildService] that returns a pre-supplied mock client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockCloudRunServicesClientBuildService : CloudRunServicesClientBuildService() {
    override fun createClient(): ServicesClient =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: ServicesClient? = null
    }
}
