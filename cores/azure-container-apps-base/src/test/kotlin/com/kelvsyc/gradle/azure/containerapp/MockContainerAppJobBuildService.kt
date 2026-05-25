package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.models.Job

/**
 * Test-only [ContainerAppJobBuildService] that returns a pre-supplied mock [Job].
 *
 * Set [mockJob] before registering this service in tests. Reset it in `afterTest` to avoid
 * cross-test pollution.
 */
abstract class MockContainerAppJobBuildService : ContainerAppJobBuildService() {
    override fun createClient(): Job =
        checkNotNull(mockJob) { "MockContainerAppJobBuildService.mockJob must be set before use" }

    companion object {
        var mockJob: Job? = null
    }
}
