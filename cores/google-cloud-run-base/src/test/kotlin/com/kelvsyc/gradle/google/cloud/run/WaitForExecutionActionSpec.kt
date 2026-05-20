package com.kelvsyc.gradle.google.cloud.run

import com.google.cloud.run.v2.Execution
import com.google.cloud.run.v2.ExecutionsClient
import com.google.protobuf.Timestamp
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.api.GradleException
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class WaitForExecutionActionSpec : FunSpec() {
    init {
        test("execute - returns when execution succeeds") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ExecutionsClient>()
            MockCloudRunExecutionsClientBuildService.mockClient = client

            val service = project.gradle.sharedServices.registerIfAbsent(
                "executions-success",
                MockCloudRunExecutionsClientBuildService::class,
            )

            val executionName = "projects/p/locations/us-central1/jobs/my-job/executions/exec-1"
            val completionTime = Timestamp.newBuilder()
                .setSeconds(1000000000L)
                .build()
            val execution = Execution.newBuilder()
                .setName(executionName)
                .setCompletionTime(completionTime)
                .setFailedCount(0)
                .setSucceededCount(1)
                .build()

            every { client.getExecution(executionName) } returns execution

            val params = project.objects.newInstance<WaitForExecutionAction.Parameters>()
            params.service.set(service)
            params.executionName.set(executionName)
            params.pollIntervalMs.set(0L)

            val action = object : WaitForExecutionAction() {
                override fun getParameters() = params
            }

            action.execute()

            verify(exactly = 1) { client.getExecution(executionName) }
        }

        test("execute - throws GradleException when execution fails") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ExecutionsClient>()
            MockCloudRunExecutionsClientBuildService.mockClient = client

            val service = project.gradle.sharedServices.registerIfAbsent(
                "executions-failure",
                MockCloudRunExecutionsClientBuildService::class,
            )

            val executionName = "projects/p/locations/us-central1/jobs/my-job/executions/exec-2"
            val completionTime = Timestamp.newBuilder()
                .setSeconds(1000000000L)
                .build()
            val execution = Execution.newBuilder()
                .setName(executionName)
                .setCompletionTime(completionTime)
                .setFailedCount(1)
                .setSucceededCount(0)
                .build()

            every { client.getExecution(executionName) } returns execution

            val params = project.objects.newInstance<WaitForExecutionAction.Parameters>()
            params.service.set(service)
            params.executionName.set(executionName)
            params.pollIntervalMs.set(0L)

            val action = object : WaitForExecutionAction() {
                override fun getParameters() = params
            }

            val exception = shouldThrow<GradleException> {
                action.execute()
            }
            exception.message shouldBe "Execution failed: $executionName"
        }

        test("execute - polls until completion") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ExecutionsClient>()
            MockCloudRunExecutionsClientBuildService.mockClient = client

            val service = project.gradle.sharedServices.registerIfAbsent(
                "executions-poll",
                MockCloudRunExecutionsClientBuildService::class,
            )

            val executionName = "projects/p/locations/us-central1/jobs/my-job/executions/exec-3"

            // First call: incomplete (no completionTime)
            val incompleteExecution = Execution.newBuilder()
                .setName(executionName)
                .setReconciling(true)
                .build()

            // Second call: complete and successful
            val completionTime = Timestamp.newBuilder()
                .setSeconds(1000000000L)
                .build()
            val completeExecution = Execution.newBuilder()
                .setName(executionName)
                .setCompletionTime(completionTime)
                .setFailedCount(0)
                .setSucceededCount(1)
                .build()

            every { client.getExecution(executionName) }
                .returnsMany(incompleteExecution, completeExecution)

            val params = project.objects.newInstance<WaitForExecutionAction.Parameters>()
            params.service.set(service)
            params.executionName.set(executionName)
            params.pollIntervalMs.set(0L)

            val action = object : WaitForExecutionAction() {
                override fun getParameters() = params
            }

            action.execute()

            verify(exactly = 2) { client.getExecution(executionName) }
        }
    }
}
