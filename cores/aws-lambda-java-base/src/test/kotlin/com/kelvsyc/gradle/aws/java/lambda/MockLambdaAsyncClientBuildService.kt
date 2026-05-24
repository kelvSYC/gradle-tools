package com.kelvsyc.gradle.aws.java.lambda

import software.amazon.awssdk.services.lambda.LambdaAsyncClient

/**
 * Test-only [LambdaAsyncClientBuildService] that returns a pre-supplied mock [LambdaAsyncClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockLambdaAsyncClientBuildService : LambdaAsyncClientBuildService() {
    override fun createClient(): LambdaAsyncClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: LambdaAsyncClient? = null
    }
}
