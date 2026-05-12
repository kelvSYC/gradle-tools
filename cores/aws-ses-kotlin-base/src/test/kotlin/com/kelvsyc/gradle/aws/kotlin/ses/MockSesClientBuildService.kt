package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient

/**
 * Test-only [SesClientBuildService] that returns a pre-supplied mock [SesClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockSesClientBuildService : SesClientBuildService() {
    override fun createClient(): SesClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: SesClient? = null
    }
}
