package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient

/**
 * Test-only [AppConfigClientBuildService] that returns a pre-supplied mock [AppConfigClient].
 */
abstract class MockAppConfigClientBuildService : AppConfigClientBuildService() {
    override fun createClient(): AppConfigClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: AppConfigClient? = null
    }
}
