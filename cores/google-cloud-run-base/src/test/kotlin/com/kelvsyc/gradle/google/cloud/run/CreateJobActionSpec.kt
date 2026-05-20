package com.kelvsyc.gradle.google.cloud.run

import com.google.api.gax.longrunning.OperationFuture
import com.google.cloud.run.v2.Job
import com.google.cloud.run.v2.JobsClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class CreateJobActionSpec : FunSpec() {
    init {
        test("execute - creates job with correct container image and env vars") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<JobsClient>()
            MockCloudRunJobsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "jobs-create",
                MockCloudRunJobsClientBuildService::class,
            )

            val jobName = "projects/p/locations/us-central1/jobs/my-job"
            val createSlot = slot<Job>()
            val parentSlot = slot<String>()
            val jobIdSlot = slot<String>()
            val operationFuture = mockk<OperationFuture<Job, Job>>()
            every { operationFuture.get() } returns Job.newBuilder()
                .setName(jobName)
                .build()
            every { client.createJobAsync(capture(parentSlot), capture(createSlot), capture(jobIdSlot)) } returns operationFuture

            val params = project.objects.newInstance<CreateJobAction.Parameters>()
            params.service.set(service)
            params.jobName.set(jobName)
            params.imageUri.set("gcr.io/p/my-image:v1")
            params.envVars.put("FOO", "bar")
            params.envVars.put("BAZ", "qux")

            val action = object : CreateJobAction() {
                override fun getParameters() = params
            }
            action.execute()

            parentSlot.captured shouldBe "projects/p/locations/us-central1"
            jobIdSlot.captured shouldBe "my-job"
            createSlot.captured.template.template.containersList shouldHaveSize 1
            createSlot.captured.template.template.containersList[0].image shouldBe "gcr.io/p/my-image:v1"
            createSlot.captured.template.template.containersList[0].envList shouldHaveSize 2
        }
    }
}
