package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRegistryClient

/**
 * Test-only [ContainerRegistryClientBuildService] that returns a pre-supplied mock client.
 */
abstract class MockContainerRegistryClientBuildService : ContainerRegistryClientBuildService() {
    override fun createClient(): ContainerRegistryClient =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: ContainerRegistryClient? = null
    }
}
