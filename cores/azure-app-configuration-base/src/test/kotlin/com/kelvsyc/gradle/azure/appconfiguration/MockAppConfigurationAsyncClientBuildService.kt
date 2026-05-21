package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.ConfigurationAsyncClient

/**
 * Test-only [AppConfigurationAsyncClientBuildService] that returns a pre-supplied mock [ConfigurationAsyncClient].
 *
 * Set [mockClient] before registering this service in tests.
 */
abstract class MockAppConfigurationAsyncClientBuildService : AppConfigurationAsyncClientBuildService() {
    override fun createClient(): ConfigurationAsyncClient =
        checkNotNull(mockClient) { "MockAppConfigurationAsyncClientBuildService.mockClient must be set before use" }

    companion object {
        var mockClient: ConfigurationAsyncClient? = null
    }
}
