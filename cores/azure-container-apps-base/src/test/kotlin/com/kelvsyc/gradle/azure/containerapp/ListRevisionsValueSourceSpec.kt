package com.kelvsyc.gradle.azure.containerapp

import com.azure.core.http.rest.PagedIterable
import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.ContainerApp
import com.azure.resourcemanager.appcontainers.models.ContainerAppsRevisions
import com.azure.resourcemanager.appcontainers.models.Revision
import com.kelvsyc.gradle.azure.containerapp.sources.ListRevisionsValueSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListRevisionsValueSourceSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppBuildService.mockApp = null
            MockContainerAppsEnvironmentBuildService.mockManager = null
        }

        test("obtain - returns list of revision names") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<ContainerAppsApiManager>()
            MockContainerAppsEnvironmentBuildService.mockManager = manager
            val envService = project.gradle.sharedServices.registerIfAbsent(
                "containerAppsEnv", MockContainerAppsEnvironmentBuildService::class,
            ) { spec ->
                spec.parameters.resourceGroupName.set("my-rg")
                spec.parameters.subscriptionId.set("sub-id")
                spec.parameters.environmentName.set("my-env")
            }

            val app = mockk<ContainerApp>()
            MockContainerAppBuildService.mockApp = app
            val appService = project.gradle.sharedServices.registerIfAbsent(
                "containerApp", MockContainerAppBuildService::class,
            ) { spec ->
                spec.parameters.environmentService.set(envService)
                spec.parameters.containerAppName.set("my-app")
            }

            val rev1 = mockk<Revision>()
            val rev2 = mockk<Revision>()
            every { rev1.name() } returns "my-app--aaa111"
            every { rev2.name() } returns "my-app--bbb222"
            val paged = mockk<PagedIterable<Revision>>()
            every { paged.iterator() } returns listOf(rev1, rev2).toMutableList().iterator()
            val revisions = mockk<ContainerAppsRevisions>()
            every { manager.containerAppsRevisions() } returns revisions
            every { revisions.listRevisions("my-rg", "my-app") } returns paged

            val provider = project.providers.ofKt(ListRevisionsValueSource::class) {
                parameters.service.set(appService)
            }
            provider.orNull shouldBe listOf("my-app--aaa111", "my-app--bbb222")
        }
    }
}
