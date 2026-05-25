package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.ContainerApp
import com.azure.resourcemanager.appcontainers.models.ContainerAppsRevisions
import com.azure.resourcemanager.appcontainers.models.Revision
import com.kelvsyc.gradle.azure.containerapp.sources.GetRevisionValueSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetRevisionValueSourceSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppBuildService.mockApp = null
            MockContainerAppsEnvironmentBuildService.mockManager = null
        }

        test("obtain - returns revision running state") {
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

            val revision = mockk<Revision>()
            val revisions = mockk<ContainerAppsRevisions>()
            every { manager.containerAppsRevisions() } returns revisions
            every { revisions.getRevision("my-rg", "my-app", "my-app--abc123") } returns revision
            every { revision.runningState().toString() } returns "Running"

            val provider = project.providers.ofKt(GetRevisionValueSource::class) {
                parameters.service.set(appService)
                parameters.revisionName.set("my-app--abc123")
            }
            provider.orNull shouldBe "Running"
        }
    }
}
