package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient

/**
 * Test-only [LambdaClientBuildService] that returns a pre-supplied mock [LambdaClient].
 */
abstract class MockLambdaClientBuildService : LambdaClientBuildService() {
    override fun createClient(): LambdaClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: LambdaClient? = null
    }
}
