package com.kelvsyc.gradle.aws.java.ssm

import software.amazon.awssdk.services.ssm.SsmClient

/**
 * Test-only [SsmClientBuildService] that returns a pre-supplied mock [SsmClient].
 */
abstract class MockSsmClientBuildService : SsmClientBuildService() {
    override fun createClient(): SsmClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: SsmClient? = null
    }
}
