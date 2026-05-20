package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.Job
import com.google.cloud.run.v2.JobsClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListJobsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns list of short job names") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<JobsClient>()
            MockCloudRunJobsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "jobs",
                MockCloudRunJobsClientBuildService::class,
            )

            val jobs = listOf(
                Job.newBuilder()
                    .setName("projects/p/locations/us-central1/jobs/job-alpha")
                    .build(),
                Job.newBuilder()
                    .setName("projects/p/locations/us-central1/jobs/job-bravo")
                    .build(),
            )
            val paged = mockk<JobsClient.ListJobsPagedResponse>()
            every { paged.iterateAll() } returns jobs

            val parentSlot = slot<String>()
            every { client.listJobs(capture(parentSlot)) } returns paged

            val provider = project.providers.ofKt(ListJobsValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("p")
                parameters.location.set("us-central1")
            }

            provider.get() shouldBe listOf("job-alpha", "job-bravo")
            parentSlot.captured shouldBe "projects/p/locations/us-central1"
        }

        test("obtain - returns empty list when no jobs exist") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<JobsClient>()
            MockCloudRunJobsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "jobs-empty",
                MockCloudRunJobsClientBuildService::class,
            )

            val paged = mockk<JobsClient.ListJobsPagedResponse>()
            every { paged.iterateAll() } returns emptyList()

            every { client.listJobs(any<String>()) } returns paged

            val provider = project.providers.ofKt(ListJobsValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("p")
                parameters.location.set("us-central1")
            }

            provider.get() shouldBe emptyList()
        }
    }
}
