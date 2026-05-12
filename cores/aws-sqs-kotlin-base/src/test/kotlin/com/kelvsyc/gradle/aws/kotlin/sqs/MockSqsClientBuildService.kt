package com.kelvsyc.gradle.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.SqsClient

/**
 * Test-only [SqsClientBuildService] that returns a pre-supplied mock [SqsClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockSqsClientBuildService : SqsClientBuildService() {
    override fun createClient(): SqsClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: SqsClient? = null
    }
}
