package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.ContainerApp
import com.azure.resourcemanager.appcontainers.models.ContainerApps
import com.kelvsyc.gradle.azure.containerapp.actions.DeleteContainerAppAction
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeleteContainerAppActionSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppBuildService.mockApp = null
            MockContainerAppsEnvironmentBuildService.mockManager = null
        }

        test("execute - deletes app by resource group and name") {
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

            val containerApps = mockk<ContainerApps>()
            every { manager.containerApps() } returns containerApps
            every { containerApps.deleteByResourceGroup(any(), any()) } returns Unit

            val params = project.objects.newInstance<DeleteContainerAppAction.Parameters>()
            params.service.set(appService)

            val action = object : DeleteContainerAppAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { containerApps.deleteByResourceGroup("my-rg", "my-app") }
        }
    }
}
