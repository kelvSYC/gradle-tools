package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.GetFunctionConfigurationRequest
import aws.sdk.kotlin.services.lambda.model.GetFunctionConfigurationResponse
import aws.sdk.kotlin.services.lambda.model.LambdaException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetFunctionConfigurationValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns function ARN on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)
            val slot = slot<GetFunctionConfigurationRequest>()
            coEvery { client.getFunctionConfiguration(capture(slot)) } returns GetFunctionConfigurationResponse {
                functionArn = "arn:aws:lambda:us-east-1:123456789012:function:my-fn:42"
            }

            val provider = project.providers.ofKt(GetFunctionConfigurationValueSource::class) {
                parameters.service.set(service)
                parameters.functionName.set("my-fn")
                parameters.qualifier.set("42")
            }
            val result = provider.get()

            result shouldBe "arn:aws:lambda:us-east-1:123456789012:function:my-fn:42"
            slot.captured.functionName shouldBe "my-fn"
            slot.captured.qualifier shouldBe "42"
        }

        test("obtain - returns null when LambdaException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<LambdaClient>()
            MockLambdaClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("lambda", MockLambdaClientBuildService::class)
            coEvery {
                client.getFunctionConfiguration(any<GetFunctionConfigurationRequest>())
            } throws LambdaException("not found")

            val provider = project.providers.ofKt(GetFunctionConfigurationValueSource::class) {
                parameters.service.set(service)
                parameters.functionName.set("missing")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
