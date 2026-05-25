package com.kelvsyc.gradle.azure.containerapp

import com.azure.core.http.rest.PagedIterable
import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.Job
import com.azure.resourcemanager.appcontainers.models.JobExecution
import com.azure.resourcemanager.appcontainers.models.JobsExecutions
import com.kelvsyc.gradle.azure.containerapp.sources.GetJobExecutionValueSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetJobExecutionValueSourceSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppJobBuildService.mockJob = null
            MockContainerAppsEnvironmentBuildService.mockManager = null
        }

        test("obtain - returns execution status") {
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

            val job = mockk<Job>()
            MockContainerAppJobBuildService.mockJob = job
            val jobService = project.gradle.sharedServices.registerIfAbsent(
                "containerAppJob", MockContainerAppJobBuildService::class,
            ) { spec ->
                spec.parameters.environmentService.set(envService)
                spec.parameters.jobName.set("my-job")
            }

            val execution = mockk<JobExecution>()
            every { execution.name() } returns "my-job--exec-abc123"
            val jobsExecutions = mockk<JobsExecutions>()
            every { manager.jobsExecutions() } returns jobsExecutions
            val paged = mockk<PagedIterable<JobExecution>>()
            every { paged.iterator() } returns listOf(execution).toMutableList().iterator()
            every { jobsExecutions.list("my-rg", "my-job") } returns paged
            every { execution.status().toString() } returns "Succeeded"

            val provider = project.providers.ofKt(GetJobExecutionValueSource::class) {
                parameters.service.set(jobService)
                parameters.executionName.set("my-job--exec-abc123")
            }
            provider.orNull shouldBe "Succeeded"
        }
    }
}
