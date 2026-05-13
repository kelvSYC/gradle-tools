package com.kelvsyc.gradle.aws.java.lambda

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.InvocationType
import software.amazon.awssdk.services.lambda.model.InvokeRequest
import software.amazon.awssdk.services.lambda.model.InvokeResponse

class InvokeFunctionActionSpec : FunSpec() {
    init {
        test("execute - passes correct invocation parameters") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)
            val requestSlot = slot<InvokeRequest>()
            every { client.invoke(capture(requestSlot)) } returns mockk<InvokeResponse>()

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
            captured.functionName() shouldBe "my-fn"
            captured.qualifier() shouldBe "prod"
            captured.payload().asUtf8String() shouldBe "{\"hello\":\"world\"}"
            captured.invocationType() shouldBe InvocationType.EVENT
        }
    }
}
