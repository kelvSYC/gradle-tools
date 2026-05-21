package com.kelvsyc.gradle.aws.java.appconfig

import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient

/**
 * Test-only [AppConfigDataClientBuildService] that returns a pre-supplied mock [AppConfigDataClient].
 */
abstract class MockAppConfigDataClientBuildService : AppConfigDataClientBuildService() {
    override fun createClient(): AppConfigDataClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: AppConfigDataClient? = null
    }
}
