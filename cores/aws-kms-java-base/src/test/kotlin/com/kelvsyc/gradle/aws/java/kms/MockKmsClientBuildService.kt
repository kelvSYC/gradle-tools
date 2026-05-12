package com.kelvsyc.gradle.aws.java.kms

import software.amazon.awssdk.services.kms.KmsClient

/**
 * Test-only [KmsClientBuildService] that returns a pre-supplied mock [KmsClient].
 */
abstract class MockKmsClientBuildService : KmsClientBuildService() {
    override fun createClient(): KmsClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: KmsClient? = null
    }
}
