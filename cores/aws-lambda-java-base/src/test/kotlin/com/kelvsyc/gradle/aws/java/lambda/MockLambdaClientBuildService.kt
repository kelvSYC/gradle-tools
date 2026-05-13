package com.kelvsyc.gradle.aws.java.lambda

import software.amazon.awssdk.services.lambda.LambdaClient

/**
 * Test-only [LambdaClientBuildService] that returns a pre-supplied mock [LambdaClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockLambdaClientBuildService : LambdaClientBuildService() {
    override fun createClient(): LambdaClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: LambdaClient? = null
    }
}
