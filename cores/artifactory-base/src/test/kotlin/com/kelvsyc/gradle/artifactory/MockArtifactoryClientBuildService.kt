package com.kelvsyc.gradle.artifactory

import org.jfrog.artifactory.client.Artifactory

/**
 * Test-only [ArtifactoryClientBuildService] that returns a pre-supplied mock [Artifactory] client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockArtifactoryClientBuildService : ArtifactoryClientBuildService() {
    override fun createClient(): Artifactory = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: Artifactory? = null
    }
}
