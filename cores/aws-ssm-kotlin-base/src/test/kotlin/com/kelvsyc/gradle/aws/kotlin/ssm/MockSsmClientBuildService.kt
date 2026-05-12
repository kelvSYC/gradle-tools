package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.SsmClient

/**
 * Test-only [SsmClientBuildService] that returns a pre-supplied mock [SsmClient].
 */
abstract class MockSsmClientBuildService : SsmClientBuildService() {
    override fun createClient(): SsmClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: SsmClient? = null
    }
}
