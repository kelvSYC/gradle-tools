package com.kelvsyc.gradle.google.cloud.functions

import com.google.cloud.functions.v2.FunctionServiceClient
import com.google.cloud.functions.v2.GetFunctionRequest
import com.google.cloud.functions.v2.ServiceConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import com.google.cloud.functions.v2.Function as GcpFunction

class GetFunctionValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns HTTPS URI when function is deployed") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<FunctionServiceClient>()
            MockFunctionServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "functions",
                MockFunctionServiceClientBuildService::class,
            )

            val uri = "https://us-central1-my-project.cloudfunctions.net/my-function"
            val function = GcpFunction.newBuilder()
                .setName("projects/my-project/locations/us-central1/functions/my-function")
                .setServiceConfig(ServiceConfig.newBuilder().setUri(uri).build())
                .build()

            val requestSlot = slot<GetFunctionRequest>()
            every { client.getFunction(capture(requestSlot)) } returns function

            val provider = project.providers.ofKt(GetFunctionValueSource::class) {
                parameters.service.set(service)
                parameters.functionName.set("projects/my-project/locations/us-central1/functions/my-function")
            }

            provider.get() shouldBe uri
            requestSlot.captured.name shouldBe "projects/my-project/locations/us-central1/functions/my-function"
        }

        test("obtain - returns null when function URI is blank") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<FunctionServiceClient>()
            MockFunctionServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "functions-blank",
                MockFunctionServiceClientBuildService::class,
            )

            val function = GcpFunction.newBuilder()
                .setName("projects/my-project/locations/us-central1/functions/my-function")
                .setServiceConfig(ServiceConfig.newBuilder().setUri("").build())
                .build()

            every { client.getFunction(any<GetFunctionRequest>()) } returns function

            val provider = project.providers.ofKt(GetFunctionValueSource::class) {
                parameters.service.set(service)
                parameters.functionName.set("projects/my-project/locations/us-central1/functions/my-function")
            }

            provider.orNull shouldBe null
        }
    }
}
