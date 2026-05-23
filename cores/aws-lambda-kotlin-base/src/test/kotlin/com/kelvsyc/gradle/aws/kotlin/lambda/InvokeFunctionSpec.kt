package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.InvocationType
import aws.sdk.kotlin.services.lambda.model.InvokeRequest
import aws.sdk.kotlin.services.lambda.model.InvokeResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class InvokeFunctionSpec : FunSpec({
    test("execute sends correct invocation parameters") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<LambdaClient>()
        MockLambdaClientBuildService.mockClient = client
        val service =
            project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)
        val requestSlot = slot<InvokeRequest>()
        coEvery { client.invoke(capture(requestSlot)) } returns mockk<InvokeResponse>()

        val task = project.tasks.create("t", InvokeFunction::class.java)
        task.service.set(service)
        task.functionName.set("my-fn")
        task.qualifier.set("prod")
        task.payload.set("{\"hello\":\"world\"}")
        task.invocationType.set("Event")

        task.execute()

        val captured = requestSlot.captured
        captured.functionName shouldBe "my-fn"
        captured.qualifier shouldBe "prod"
        captured.payload?.toString(Charsets.UTF_8) shouldBe "{\"hello\":\"world\"}"
        captured.invocationType shouldBe InvocationType.Event
        MockLambdaClientBuildService.mockClient = null
    }

    test("execute uses default invocation type when omitted") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<LambdaClient>()
        MockLambdaClientBuildService.mockClient = client
        val service =
            project.gradle.sharedServices.registerIfAbsent("lambda2", MockLambdaClientBuildService::class)
        coEvery { client.invoke(any()) } returns mockk<InvokeResponse>()

        val task = project.tasks.create("t2", InvokeFunction::class.java)
        task.service.set(service)
        task.functionName.set("my-fn")

        task.execute()

        coVerify { client.invoke(any()) }
        MockLambdaClientBuildService.mockClient = null
    }
})
