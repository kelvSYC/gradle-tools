package com.kelvsyc.gradle.azure.containerapp

import com.azure.core.http.rest.PagedIterable
import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.Job
import com.azure.resourcemanager.appcontainers.models.Jobs
import com.kelvsyc.gradle.azure.containerapp.sources.ListContainerAppJobsValueSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListContainerAppJobsValueSourceSpec : FunSpec() {
    init {
        afterTest { MockContainerAppsEnvironmentBuildService.mockManager = null }

        test("obtain - returns list of job names") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<ContainerAppsApiManager>()
            MockContainerAppsEnvironmentBuildService.mockManager = manager
            val service = project.gradle.sharedServices.registerIfAbsent(
                "containerAppsEnv", MockContainerAppsEnvironmentBuildService::class,
            ) { spec ->
                spec.parameters.resourceGroupName.set("my-rg")
                spec.parameters.environmentName.set("my-env")
                spec.parameters.subscriptionId.set("sub-id")
            }

            val job1 = mockk<Job>()
            val job2 = mockk<Job>()
            every { job1.name() } returns "job-one"
            every { job2.name() } returns "job-two"
            val paged = mockk<PagedIterable<Job>>()
            every { paged.iterator() } returns listOf(job1, job2).toMutableList().iterator()
            val jobs = mockk<Jobs>()
            every { manager.jobs() } returns jobs
            every { jobs.listByResourceGroup("my-rg") } returns paged

            val provider = project.providers.ofKt(ListContainerAppJobsValueSource::class) {
                parameters.service.set(service)
            }
            provider.orNull shouldBe listOf("job-one", "job-two")
        }
    }
}
