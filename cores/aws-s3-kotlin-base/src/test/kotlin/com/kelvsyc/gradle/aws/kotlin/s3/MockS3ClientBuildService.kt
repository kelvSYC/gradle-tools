package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client

/**
 * Test-only [S3ClientBuildService] that returns a pre-supplied mock [S3Client].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockS3ClientBuildService : S3ClientBuildService() {
    override fun createClient(): S3Client = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: S3Client? = null
    }
}
