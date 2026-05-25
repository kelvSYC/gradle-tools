package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironments
import com.kelvsyc.gradle.azure.containerapp.actions.DeleteManagedEnvironmentAction
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeleteManagedEnvironmentActionSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppsEnvironmentBuildService.mockManager = null
        }

        test("execute - deletes environment by resource group and name") {
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

            val managedEnvs = mockk<ManagedEnvironments>()
            every { manager.managedEnvironments() } returns managedEnvs
            every { managedEnvs.deleteByResourceGroup(any(), any()) } returns Unit

            val params = project.objects.newInstance<DeleteManagedEnvironmentAction.Parameters>()
            params.service.set(service)

            val action = object : DeleteManagedEnvironmentAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { managedEnvs.deleteByResourceGroup("my-rg", "my-env") }
        }
    }
}
