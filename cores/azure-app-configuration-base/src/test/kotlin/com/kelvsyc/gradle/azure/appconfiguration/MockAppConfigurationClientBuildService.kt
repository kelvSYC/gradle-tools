package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.ConfigurationClient

/**
 * Test-only [AppConfigurationClientBuildService] that returns a pre-supplied mock [ConfigurationClient].
 *
 * Set [mockClient] before registering this service in tests.
 */
abstract class MockAppConfigurationClientBuildService : AppConfigurationClientBuildService() {
    override fun createClient(): ConfigurationClient =
        checkNotNull(mockClient) { "MockAppConfigurationClientBuildService.mockClient must be set before use" }

    companion object {
        var mockClient: ConfigurationClient? = null
    }
}
