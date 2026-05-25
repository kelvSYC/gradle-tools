package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager

/**
 * Test-only [ContainerAppsEnvironmentBuildService] that returns a pre-supplied mock manager.
 *
 * Set [mockManager] before registering this service in tests. Reset it in `afterTest` to avoid
 * cross-test pollution.
 */
abstract class MockContainerAppsEnvironmentBuildService : ContainerAppsEnvironmentBuildService() {
    override fun createClient(): ContainerAppsApiManager =
        checkNotNull(mockManager) { "MockContainerAppsEnvironmentBuildService.mockManager must be set before use" }

    companion object {
        var mockManager: ContainerAppsApiManager? = null
    }
}
