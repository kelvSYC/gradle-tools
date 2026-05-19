package com.kelvsyc.gradle.google.cloud.functions

import com.google.cloud.functions.v2.FunctionServiceClient
import com.google.cloud.functions.v2.ListFunctionsRequest
import com.google.cloud.functions.v2.ServiceConfig
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import com.google.cloud.functions.v2.Function as GcpFunction

class ListFunctionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns map of short name to HTTPS URI") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<FunctionServiceClient>()
            MockFunctionServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "functions",
                MockFunctionServiceClientBuildService::class,
            )

            val functions = listOf(
                GcpFunction.newBuilder()
                    .setName("projects/p/locations/us-central1/functions/alpha")
                    .setServiceConfig(ServiceConfig.newBuilder().setUri("https://alpha.example.com").build())
                    .build(),
                GcpFunction.newBuilder()
                    .setName("projects/p/locations/us-central1/functions/bravo")
                    .setServiceConfig(ServiceConfig.newBuilder().setUri("https://bravo.example.com").build())
                    .build(),
            )
            val paged = mockk<FunctionServiceClient.ListFunctionsPagedResponse>()
            every { paged.iterateAll() } returns functions

            val requestSlot = slot<ListFunctionsRequest>()
            every { client.listFunctions(capture(requestSlot)) } returns paged

            val provider = project.providers.ofKt(ListFunctionsValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("p")
                parameters.location.set("us-central1")
            }

            provider.get() shouldBe mapOf(
                "alpha" to "https://alpha.example.com",
                "bravo" to "https://bravo.example.com",
            )
            requestSlot.captured.parent shouldBe "projects/p/locations/us-central1"
        }
    }
}
