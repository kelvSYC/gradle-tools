package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.Job
import com.azure.resourcemanager.appcontainers.models.Jobs
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironments
import com.kelvsyc.gradle.azure.containerapp.actions.CreateContainerAppJobAction
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class CreateContainerAppJobActionSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppsEnvironmentBuildService.mockManager = null
        }

        test("execute - defines job with given name") {
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

            val jobs = mockk<Jobs>()
            val managedEnvs = mockk<ManagedEnvironments>(relaxed = true)
            val blankDef = mockk<Job.DefinitionStages.Blank>(relaxed = true)
            every { manager.jobs() } returns jobs
            every { manager.managedEnvironments() } returns managedEnvs
            every { jobs.define(any()) } returns blankDef
            every { blankDef.withRegion(any<String>()) } returns mockk(relaxed = true)

            val params = project.objects.newInstance<CreateContainerAppJobAction.Parameters>()
            params.service.set(service)
            params.jobName.set("my-job")
            params.imageUri.set("mcr.microsoft.com/azuredocs/containerapps-helloworld:latest")
            params.location.set("eastus")
            params.triggerType.set(JobTriggerType.MANUAL)

            val action = object : CreateContainerAppJobAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { jobs.define("my-job") }
        }
    }
}
