package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.ContainerApp
import com.azure.resourcemanager.appcontainers.models.ContainerApps
import com.kelvsyc.gradle.azure.containerapp.actions.CreateContainerAppAction
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class CreateContainerAppActionSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppsEnvironmentBuildService.mockManager = null
        }

        test("execute - defines container app with given name") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<ContainerAppsApiManager>()
            MockContainerAppsEnvironmentBuildService.mockManager = manager
            val service = project.gradle.sharedServices.registerIfAbsent(
                "containerAppsEnv",
                MockContainerAppsEnvironmentBuildService::class,
            ) { spec ->
                spec.parameters.resourceGroupName.set("my-rg")
                spec.parameters.environmentName.set("my-env")
                spec.parameters.subscriptionId.set("sub-id")
            }

            val containerApps = mockk<ContainerApps>()
            val blankDef = mockk<ContainerApp.DefinitionStages.Blank>(relaxed = true)
            every { manager.containerApps() } returns containerApps
            every { containerApps.define(any()) } returns blankDef
            every { blankDef.withRegion(any<String>()) } returns mockk(relaxed = true)
            val managedEnvs = mockk<com.azure.resourcemanager.appcontainers.models.ManagedEnvironments>(relaxed = true)
            every { manager.managedEnvironments() } returns managedEnvs

            val params = project.objects.newInstance<CreateContainerAppAction.Parameters>()
            params.service.set(service)
            params.containerAppName.set("my-app")
            params.imageUri.set("mcr.microsoft.com/azuredocs/containerapps-helloworld:latest")
            params.location.set("eastus")
            params.ingressEnabled.set(true)
            params.targetPort.set(80)
            params.minReplicas.set(0)
            params.maxReplicas.set(10)

            val action = object : CreateContainerAppAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { containerApps.define("my-app") }
        }
    }
}
