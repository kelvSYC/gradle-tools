package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironment
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironments
import com.kelvsyc.gradle.clients.CredentialReference
import com.kelvsyc.gradle.azure.containerapp.actions.CreateManagedEnvironmentAction
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class CreateManagedEnvironmentActionSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppsEnvironmentBuildService.mockManager = null
        }

        test("execute - creates managed environment with Log Analytics config") {
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

            val managedEnvDef = mockk<ManagedEnvironment.DefinitionStages.Blank>(relaxed = true)
            val managedEnvs = mockk<ManagedEnvironments>()
            every { manager.managedEnvironments() } returns managedEnvs
            every { managedEnvs.define(any()) } returns managedEnvDef
            every { managedEnvDef.withRegion(any<String>()) } returns mockk(relaxed = true)

            val params = project.objects.newInstance<CreateManagedEnvironmentAction.Parameters>()
            params.service.set(service)
            params.location.set("eastus")
            params.logAnalyticsWorkspaceId.set("ws-id")
            params.logAnalyticsWorkspaceKey.set(CredentialReference.Literal("ws-key"))

            val action = object : CreateManagedEnvironmentAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { managedEnvs.define("my-env") }
        }
    }
}
