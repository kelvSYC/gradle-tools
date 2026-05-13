package com.kelvsyc.gradle.aws.kotlin.imds

import aws.sdk.kotlin.runtime.config.imds.ImdsClient

/**
 * Test-only [ImdsClientBuildService] that returns a pre-supplied mock [ImdsClient].
 */
abstract class MockImdsClientBuildService : ImdsClientBuildService() {
    override fun createClient(): ImdsClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: ImdsClient? = null
    }
}
