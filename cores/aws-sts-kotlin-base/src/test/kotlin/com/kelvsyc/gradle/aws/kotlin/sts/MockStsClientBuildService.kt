package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.services.BuildServiceParameters

/**
 * Test-only build service that returns a pre-supplied mock [StsClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockStsClientBuildService :
    AbstractClientBuildService<StsClient, BuildServiceParameters.None>() {
    override fun createClient(): StsClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: StsClient? = null
    }
}
