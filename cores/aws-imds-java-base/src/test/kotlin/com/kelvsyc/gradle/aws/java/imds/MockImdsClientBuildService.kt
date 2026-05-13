package com.kelvsyc.gradle.aws.java.imds

import software.amazon.awssdk.imds.Ec2MetadataClient

/**
 * Test-only [ImdsClientBuildService] that returns a pre-supplied mock [Ec2MetadataClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockImdsClientBuildService : ImdsClientBuildService() {
    override fun createClient(): Ec2MetadataClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: Ec2MetadataClient? = null
    }
}
