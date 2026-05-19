package com.kelvsyc.gradle.azure.functions

import com.azure.resourcemanager.appservice.AppServiceManager

/**
 * Test-only [FunctionAppClientBuildService] that returns a pre-supplied mock [AppServiceManager].
 */
abstract class MockFunctionAppClientBuildService : FunctionAppClientBuildService() {
    override fun createClient(): AppServiceManager =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: AppServiceManager? = null
    }
}
