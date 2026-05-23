package com.kelvsyc.gradle.aws.java.codeartifact

import software.amazon.awssdk.services.codeartifact.CodeartifactAsyncClient

/**
 * Test-only [CodeArtifactAsyncClientBuildService] that returns a pre-supplied mock [CodeartifactAsyncClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockCodeArtifactAsyncClientBuildService : CodeArtifactAsyncClientBuildService() {
    override fun createClient(): CodeartifactAsyncClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: CodeartifactAsyncClient? = null
    }
}
