package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.models.ContainerApp

/**
 * Test-only [ContainerAppBuildService] that returns a pre-supplied mock [ContainerApp].
 *
 * Set [mockApp] before registering this service in tests. Reset it in `afterTest` to avoid
 * cross-test pollution.
 */
abstract class MockContainerAppBuildService : ContainerAppBuildService() {
    override fun createClient(): ContainerApp =
        checkNotNull(mockApp) { "MockContainerAppBuildService.mockApp must be set before use" }

    companion object {
        var mockApp: ContainerApp? = null
    }
}
