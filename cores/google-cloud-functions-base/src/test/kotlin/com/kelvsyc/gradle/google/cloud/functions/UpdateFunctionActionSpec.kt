package com.kelvsyc.gradle.google.cloud.functions

import com.google.api.gax.longrunning.OperationFuture
import com.google.cloud.functions.v2.FunctionServiceClient
import com.google.cloud.functions.v2.GetFunctionRequest
import com.google.cloud.functions.v2.OperationMetadata
import com.google.cloud.functions.v2.UpdateFunctionRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import com.google.cloud.functions.v2.Function as GcpFunction

class UpdateFunctionActionSpec : FunSpec() {
    init {
        test("execute - updates function source with correct GCS bucket and object") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<FunctionServiceClient>()
            MockFunctionServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "functions",
                MockFunctionServiceClientBuildService::class,
            )

            val existingFunction = GcpFunction.newBuilder()
                .setName("projects/p/locations/us-central1/functions/my-fn")
                .build()
            every { client.getFunction(any<GetFunctionRequest>()) } returns existingFunction

            val updateSlot = slot<UpdateFunctionRequest>()
            val operationFuture = mockk<OperationFuture<GcpFunction, OperationMetadata>>()
            every { operationFuture.get() } returns existingFunction
            every { client.updateFunctionAsync(capture(updateSlot)) } returns operationFuture

            val params = project.objects.newInstance<UpdateFunctionAction.Parameters>()
            params.service.set(service)
            params.functionName.set("projects/p/locations/us-central1/functions/my-fn")
            params.bucket.set("my-bucket")
            params.storageObject.set("deployments/function.zip")

            val action = object : UpdateFunctionAction() {
                override fun getParameters() = params
            }
            action.execute()

            val source = updateSlot.captured.function.buildConfig.source.storageSource
            source.bucket shouldBe "my-bucket"
            source.getObject() shouldBe "deployments/function.zip"
            source.generation shouldBe 0L
            updateSlot.captured.updateMask.pathsList shouldBe listOf("build_config.source")
        }

        test("execute - pins GCS object generation when storageGeneration is set") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<FunctionServiceClient>()
            MockFunctionServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "functions-gen",
                MockFunctionServiceClientBuildService::class,
            )

            val existingFunction = GcpFunction.newBuilder()
                .setName("projects/p/locations/us-central1/functions/my-fn")
                .build()
            every { client.getFunction(any<GetFunctionRequest>()) } returns existingFunction

            val updateSlot = slot<UpdateFunctionRequest>()
            val operationFuture = mockk<OperationFuture<GcpFunction, OperationMetadata>>()
            every { operationFuture.get() } returns existingFunction
            every { client.updateFunctionAsync(capture(updateSlot)) } returns operationFuture

            val params = project.objects.newInstance<UpdateFunctionAction.Parameters>()
            params.service.set(service)
            params.functionName.set("projects/p/locations/us-central1/functions/my-fn")
            params.bucket.set("my-bucket")
            params.storageObject.set("deployments/function.zip")
            params.storageGeneration.set(42L)

            val action = object : UpdateFunctionAction() {
                override fun getParameters() = params
            }
            action.execute()

            updateSlot.captured.function.buildConfig.source.storageSource.generation shouldBe 42L
        }
    }
}
