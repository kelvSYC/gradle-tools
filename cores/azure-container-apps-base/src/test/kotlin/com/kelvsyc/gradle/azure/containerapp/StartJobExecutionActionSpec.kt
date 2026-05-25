package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.Job
import com.azure.resourcemanager.appcontainers.models.JobExecutionBase
import com.azure.resourcemanager.appcontainers.models.Jobs
import com.kelvsyc.gradle.azure.containerapp.actions.StartJobExecutionAction
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class StartJobExecutionActionSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppJobBuildService.mockJob = null
            MockContainerAppsEnvironmentBuildService.mockManager = null
        }

        test("execute - starts job and writes execution name to file") {
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

            val executionBase = mockk<JobExecutionBase>()
            val jobs = mockk<Jobs>()
            every { manager.jobs() } returns jobs
            every { jobs.start("my-rg", "my-job") } returns executionBase
            every { executionBase.name() } returns "my-job--exec-abc123"

            val outputFile = File.createTempFile("execution", ".txt").also { it.deleteOnExit() }
            val params = project.objects.newInstance<StartJobExecutionAction.Parameters>()
            params.service.set(jobService)
            params.executionNameFile.set(outputFile)

            val action = object : StartJobExecutionAction() {
                override fun getParameters() = params
            }
            action.execute()

            outputFile.readText() shouldBe "my-job--exec-abc123"
        }
    }
}
