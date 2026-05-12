package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient

/**
 * Test-only [EcrClientBuildService] that returns a pre-supplied mock [EcrClient].
 */
abstract class MockEcrClientBuildService : EcrClientBuildService() {
    override fun createClient(): EcrClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: EcrClient? = null
    }
}
