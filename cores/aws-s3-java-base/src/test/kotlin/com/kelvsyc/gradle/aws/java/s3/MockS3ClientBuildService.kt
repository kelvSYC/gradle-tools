package com.kelvsyc.gradle.aws.java.s3

import software.amazon.awssdk.services.s3.S3Client

/**
 * Test-only [S3ClientBuildService] that returns a pre-supplied mock [S3Client].
 */
abstract class MockS3ClientBuildService : S3ClientBuildService() {
    override fun createClient(): S3Client = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: S3Client? = null
    }
}
