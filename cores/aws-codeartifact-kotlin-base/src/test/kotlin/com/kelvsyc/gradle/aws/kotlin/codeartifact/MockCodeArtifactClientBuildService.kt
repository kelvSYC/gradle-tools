package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient

/**
 * Test-only [CodeArtifactClientBuildService] that returns a pre-supplied mock [CodeartifactClient].
 */
abstract class MockCodeArtifactClientBuildService : CodeArtifactClientBuildService() {
    override fun createClient(): CodeartifactClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: CodeartifactClient? = null
    }
}
