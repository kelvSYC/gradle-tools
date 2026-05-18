package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRepository

/**
 * Test-only [ContainerRepositoryClientBuildService] that returns a pre-supplied mock client.
 */
abstract class MockContainerRepositoryClientBuildService : ContainerRepositoryClientBuildService() {
    override fun createClient(): ContainerRepository =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: ContainerRepository? = null
    }
}
