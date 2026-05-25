package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.ContainerApp
import com.azure.resourcemanager.appcontainers.models.ContainerAppsRevisions
import com.kelvsyc.gradle.azure.containerapp.actions.DeactivateRevisionAction
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeactivateRevisionActionSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppBuildService.mockApp = null
            MockContainerAppsEnvironmentBuildService.mockManager = null
        }

        test("execute - deactivates revision by name") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<ContainerAppsApiManager>()
            MockContainerAppsEnvironmentBuildService.mockManager = manager
            val envService = project.gradle.sharedServices.registerIfAbsent(
                "containerAppsEnv",
                MockContainerAppsEnvironmentBuildService::class,
            ) { spec ->
                spec.parameters.resourceGroupName.set("my-rg")
                spec.parameters.subscriptionId.set("sub-id")
                spec.parameters.environmentName.set("my-env")
            }

            val app = mockk<ContainerApp>()
            MockContainerAppBuildService.mockApp = app
            val appService = project.gradle.sharedServices.registerIfAbsent(
                "containerApp",
                MockContainerAppBuildService::class,
            ) { spec ->
                spec.parameters.environmentService.set(envService)
                spec.parameters.containerAppName.set("my-app")
            }

            val revisions = mockk<ContainerAppsRevisions>()
            every { manager.containerAppsRevisions() } returns revisions
            every { revisions.deactivateRevision(any(), any(), any()) } returns Unit

            val params = project.objects.newInstance<DeactivateRevisionAction.Parameters>()
            params.service.set(appService)
            params.revisionName.set("my-app--abc123")

            val action = object : DeactivateRevisionAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { revisions.deactivateRevision("my-rg", "my-app", "my-app--abc123") }
        }
    }
}
