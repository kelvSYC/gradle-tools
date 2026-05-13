package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.InvocationType
import aws.sdk.kotlin.services.lambda.model.InvokeRequest
import aws.sdk.kotlin.services.lambda.model.InvokeResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class InvokeFunctionActionSpec : FunSpec() {
    init {
        test("execute - passes correct invocation parameters") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)
            val requestSlot = slot<InvokeRequest>()
            coEvery { client.invoke(capture(requestSlot)) } returns mockk<InvokeResponse>()

            val params = project.objects.newInstance<InvokeFunctionAction.Parameters>()
            params.service.set(service)
            params.functionName.set("my-fn")
            params.qualifier.set("prod")
            params.payload.set("{\"hello\":\"world\"}")
            params.invocationType.set("Event")

            val action = object : InvokeFunctionAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = requestSlot.captured
            captured.functionName shouldBe "my-fn"
            captured.qualifier shouldBe "prod"
            captured.payload?.toString(Charsets.UTF_8) shouldBe "{\"hello\":\"world\"}"
            captured.invocationType shouldBe InvocationType.Event
        }
    }
}
