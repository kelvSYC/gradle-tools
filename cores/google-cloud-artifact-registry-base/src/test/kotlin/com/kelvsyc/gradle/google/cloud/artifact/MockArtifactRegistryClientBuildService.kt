package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient

/**
 * Test-only [ArtifactRegistryClientBuildService] that returns a pre-supplied mock client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockArtifactRegistryClientBuildService : ArtifactRegistryClientBuildService() {
    override fun createClient(): ArtifactRegistryClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: ArtifactRegistryClient? = null
    }
}
