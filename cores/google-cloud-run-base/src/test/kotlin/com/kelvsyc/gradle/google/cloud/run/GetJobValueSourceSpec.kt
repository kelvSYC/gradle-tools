package com.kelvsyc.gradle.google.cloud.run

import com.google.api.gax.rpc.NotFoundException
import com.google.api.gax.rpc.StatusCode
import com.google.cloud.run.v2.ExecutionReference
import com.google.cloud.run.v2.Job
import com.google.cloud.run.v2.JobsClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetJobValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns latest execution name when job exists") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<JobsClient>()
            MockCloudRunJobsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "jobs",
                MockCloudRunJobsClientBuildService::class,
            )

            val executionName = "projects/my-project/locations/us-central1/jobs/my-job/executions/abc123"
            val latestExecution = ExecutionReference.newBuilder()
                .setName(executionName)
                .build()

            val jobProto = Job.newBuilder()
                .setName("projects/my-project/locations/us-central1/jobs/my-job")
                .setLatestCreatedExecution(latestExecution)
                .build()

            val nameSlot = slot<String>()
            every { client.getJob(capture(nameSlot)) } returns jobProto

            val provider = project.providers.ofKt(GetJobValueSource::class) {
                parameters.service.set(service)
                parameters.jobName.set("projects/my-project/locations/us-central1/jobs/my-job")
            }

            provider.get() shouldBe executionName
            nameSlot.captured shouldBe "projects/my-project/locations/us-central1/jobs/my-job"
        }

        test("obtain - returns null when latest execution name is blank") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<JobsClient>()
            MockCloudRunJobsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "jobs-blank",
                MockCloudRunJobsClientBuildService::class,
            )

            val latestExecution = ExecutionReference.newBuilder()
                .setName("")
                .build()

            val jobProto = Job.newBuilder()
                .setName("projects/my-project/locations/us-central1/jobs/my-job")
                .setLatestCreatedExecution(latestExecution)
                .build()

            every { client.getJob(any<String>()) } returns jobProto

            val provider = project.providers.ofKt(GetJobValueSource::class) {
                parameters.service.set(service)
                parameters.jobName.set("projects/my-project/locations/us-central1/jobs/my-job")
            }

            provider.orNull shouldBe null
        }

        test("obtain - returns null when job is not found") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<JobsClient>()
            MockCloudRunJobsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "jobs-not-found",
                MockCloudRunJobsClientBuildService::class,
            )

            val statusCode = mockk<StatusCode>()
            every { client.getJob(any<String>()) } throws NotFoundException(
                Exception("Job not found"),
                statusCode,
                false
            )

            val provider = project.providers.ofKt(GetJobValueSource::class) {
                parameters.service.set(service)
                parameters.jobName.set("projects/my-project/locations/us-central1/jobs/nonexistent")
            }

            provider.orNull shouldBe null
        }
    }
}
