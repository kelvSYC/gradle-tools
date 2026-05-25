package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.Job
import com.azure.resourcemanager.appcontainers.models.Jobs
import com.kelvsyc.gradle.azure.containerapp.actions.DeleteContainerAppJobAction
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeleteContainerAppJobActionSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppJobBuildService.mockJob = null
            MockContainerAppsEnvironmentBuildService.mockManager = null
        }

        test("execute - deletes job by resource group and name") {
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

            val job = mockk<Job>()
            MockContainerAppJobBuildService.mockJob = job
            val jobService = project.gradle.sharedServices.registerIfAbsent(
                "containerAppJob",
                MockContainerAppJobBuildService::class,
            ) { spec ->
                spec.parameters.environmentService.set(envService)
                spec.parameters.jobName.set("my-job")
            }

            val jobs = mockk<Jobs>()
            every { manager.jobs() } returns jobs
            every { jobs.deleteByResourceGroup(any(), any()) } returns Unit

            val params = project.objects.newInstance<DeleteContainerAppJobAction.Parameters>()
            params.service.set(jobService)

            val action = object : DeleteContainerAppJobAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { jobs.deleteByResourceGroup("my-rg", "my-job") }
        }
    }
}
