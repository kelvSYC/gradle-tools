package com.kelvsyc.gradle.google.cloud.run

import com.google.api.gax.longrunning.OperationFuture
import com.google.cloud.run.v2.Job
import com.google.cloud.run.v2.JobsClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeleteJobActionSpec : FunSpec() {
    init {
        test("execute - calls deleteJobAsync with the correct job name") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<JobsClient>()
            MockCloudRunJobsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "jobs-delete",
                MockCloudRunJobsClientBuildService::class,
            )

            val jobName = "projects/p/locations/us-central1/jobs/my-job"
            val nameSlot = slot<String>()
            val operationFuture = mockk<OperationFuture<Job, Job>>()
            every { operationFuture.get() } returns Job.newBuilder()
                .setName(jobName)
                .build()
            every { client.deleteJobAsync(capture(nameSlot)) } returns operationFuture

            val params = project.objects.newInstance<DeleteJobAction.Parameters>()
            params.service.set(service)
            params.jobName.set(jobName)

            val action = object : DeleteJobAction() {
                override fun getParameters() = params
            }
            action.execute()

            nameSlot.captured shouldBe jobName
        }
    }
}
