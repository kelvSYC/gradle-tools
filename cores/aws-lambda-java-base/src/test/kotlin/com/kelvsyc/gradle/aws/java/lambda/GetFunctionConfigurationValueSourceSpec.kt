package com.kelvsyc.gradle.aws.java.lambda

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.GetFunctionConfigurationRequest
import software.amazon.awssdk.services.lambda.model.GetFunctionConfigurationResponse
import software.amazon.awssdk.services.lambda.model.LambdaException

class GetFunctionConfigurationValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns function ARN on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)
            val slot = slot<GetFunctionConfigurationRequest>()
            val response = mockk<GetFunctionConfigurationResponse>()
            every { response.functionArn() } returns "arn:aws:lambda:us-east-1:123:function:my-fn:42"
            every { client.getFunctionConfiguration(capture(slot)) } returns response

            val provider = project.providers.ofKt(GetFunctionConfigurationValueSource::class) {
                parameters.service.set(service)
                parameters.functionName.set("my-fn")
                parameters.qualifier.set("42")
            }
            val result = provider.get()

            result shouldBe "arn:aws:lambda:us-east-1:123:function:my-fn:42"
            slot.captured.functionName() shouldBe "my-fn"
            slot.captured.qualifier() shouldBe "42"
        }

        test("obtain - returns null when LambdaException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)
            every { client.getFunctionConfiguration(any<GetFunctionConfigurationRequest>()) } throws LambdaException.builder().message("not found").build()

            val provider = project.providers.ofKt(GetFunctionConfigurationValueSource::class) {
                parameters.service.set(service)
                parameters.functionName.set("missing")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
