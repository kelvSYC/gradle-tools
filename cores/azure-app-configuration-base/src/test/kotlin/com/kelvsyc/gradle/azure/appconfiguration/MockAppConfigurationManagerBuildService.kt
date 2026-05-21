package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.resourcemanager.appconfiguration.AppConfigurationManager

/**
 * Test-only [AppConfigurationManagerBuildService] that returns a pre-supplied mock [AppConfigurationManager].
 *
 * Set [mockClient] before registering this service in tests.
 */
abstract class MockAppConfigurationManagerBuildService : AppConfigurationManagerBuildService() {
    override fun createClient(): AppConfigurationManager =
        checkNotNull(mockClient) { "MockAppConfigurationManagerBuildService.mockClient must be set before use" }

    companion object {
        var mockClient: AppConfigurationManager? = null
    }
}
