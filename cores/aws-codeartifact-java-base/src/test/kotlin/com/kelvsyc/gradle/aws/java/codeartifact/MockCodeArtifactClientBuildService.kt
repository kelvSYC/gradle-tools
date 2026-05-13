package com.kelvsyc.gradle.aws.java.codeartifact

import software.amazon.awssdk.services.codeartifact.CodeartifactClient

/**
 * Test-only [CodeArtifactClientBuildService] that returns a pre-supplied mock [CodeartifactClient].
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockCodeArtifactClientBuildService : CodeArtifactClientBuildService() {
    override fun createClient(): CodeartifactClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: CodeartifactClient? = null
    }
}
