package com.kelvsyc.gradle.google.cloud.run

import com.google.api.gax.longrunning.OperationFuture
import com.google.cloud.run.v2.Job
import com.google.cloud.run.v2.JobsClient
import com.google.cloud.run.v2.UpdateJobRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UpdateJobActionSpec : FunSpec() {
    init {
        test("execute - updates existing job with new image and env vars") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<JobsClient>()
            MockCloudRunJobsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "jobs-update",
                MockCloudRunJobsClientBuildService::class,
            )

            val jobName = "projects/p/locations/us-central1/jobs/my-job"
            val existingJob = Job.newBuilder()
                .setName(jobName)
                .build()
            every { client.getJob(jobName) } returns existingJob

            val requestSlot = slot<UpdateJobRequest>()
            val operationFuture = mockk<OperationFuture<Job, Job>>()
            every { operationFuture.get() } returns existingJob
            every { client.updateJobAsync(capture(requestSlot)) } returns operationFuture

            val params = project.objects.newInstance<UpdateJobAction.Parameters>()
            params.service.set(service)
            params.jobName.set(jobName)
            params.imageUri.set("gcr.io/p/my-image:v2")
            params.envVars.put("ENV", "production")

            val action = object : UpdateJobAction() {
                override fun getParameters() = params
            }
            action.execute()

            val capturedJob = requestSlot.captured.job
            capturedJob.template.template.containersList shouldHaveSize 1
            capturedJob.template.template.containersList[0].image shouldBe "gcr.io/p/my-image:v2"
            capturedJob.template.template.containersList[0].envList shouldHaveSize 1
            capturedJob.template.template.containersList[0].envList[0].name shouldBe "ENV"
            capturedJob.template.template.containersList[0].envList[0].value shouldBe "production"
        }

        test("execute - update passes correct request") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<JobsClient>()
            MockCloudRunJobsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "jobs-mask",
                MockCloudRunJobsClientBuildService::class,
            )

            val jobName = "projects/p/locations/us-central1/jobs/my-job"
            val existingJob = Job.newBuilder()
                .setName(jobName)
                .build()
            every { client.getJob(jobName) } returns existingJob

            val requestSlot = slot<UpdateJobRequest>()
            val operationFuture = mockk<OperationFuture<Job, Job>>()
            every { operationFuture.get() } returns existingJob
            every { client.updateJobAsync(capture(requestSlot)) } returns operationFuture

            val params = project.objects.newInstance<UpdateJobAction.Parameters>()
            params.service.set(service)
            params.jobName.set(jobName)
            params.imageUri.set("gcr.io/p/my-image:v3")

            val action = object : UpdateJobAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.job.template.template.containersList shouldHaveSize 1
            requestSlot.captured.job.template.template.containersList[0].image shouldBe "gcr.io/p/my-image:v3"
        }
    }
}
