package com.kelvsyc.gradle.azure.containerapp

import com.azure.core.http.rest.PagedIterable
import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.ContainerApp
import com.azure.resourcemanager.appcontainers.models.ContainerApps
import com.kelvsyc.gradle.azure.containerapp.sources.ListContainerAppsValueSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListContainerAppsValueSourceSpec : FunSpec() {
    init {
        afterTest { MockContainerAppsEnvironmentBuildService.mockManager = null }

        test("obtain - returns list of app names") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<ContainerAppsApiManager>()
            MockContainerAppsEnvironmentBuildService.mockManager = manager
            val service = project.gradle.sharedServices.registerIfAbsent(
                "containerAppsEnv", MockContainerAppsEnvironmentBuildService::class,
            ) { spec ->
                spec.parameters.resourceGroupName.set("my-rg")
                spec.parameters.environmentName.set("my-env")
                spec.parameters.subscriptionId.set("sub-id")
            }

            val app1 = mockk<ContainerApp>()
            val app2 = mockk<ContainerApp>()
            every { app1.name() } returns "app-one"
            every { app2.name() } returns "app-two"
            val pagedIterable = mockk<PagedIterable<ContainerApp>>()
            every { pagedIterable.iterator() } returns listOf(app1, app2).toMutableList().iterator()
            val containerApps = mockk<ContainerApps>()
            every { manager.containerApps() } returns containerApps
            every { containerApps.listByResourceGroup("my-rg") } returns pagedIterable

            val provider = project.providers.ofKt(ListContainerAppsValueSource::class) {
                parameters.service.set(service)
            }
            provider.orNull shouldBe listOf("app-one", "app-two")
        }
    }
}
