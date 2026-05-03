package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import aws.sdk.kotlin.services.lambda.model.GetFunctionConfigurationRequest
import aws.sdk.kotlin.services.lambda.model.GetFunctionConfigurationResponse
import aws.sdk.kotlin.services.lambda.model.LambdaException
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.lambda.MockLambdaClientInfoInternal
import com.kelvsyc.gradle.plugins.LambdaKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class GetFunctionConfigurationValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns function ARN on success") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(LambdaKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockLambdaClientInfo::class, MockLambdaClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockLambdaClientInfo>("mock") {}
            val slot = slot<GetFunctionConfigurationRequest>()
            val client = extension.getClient<LambdaClient, MockLambdaClientInfo>("mock").get()!!
            coEvery { client.getFunctionConfiguration(capture(slot)) } returns GetFunctionConfigurationResponse {
                functionArn = "arn:aws:lambda:us-east-1:123456789012:function:my-fn:42"
            }

            val provider = project.providers.of(GetFunctionConfigurationValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
            project.pluginManager.apply(LambdaKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockLambdaClientInfo::class, MockLambdaClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockLambdaClientInfo>("mock") {}
            val client = extension.getClient<LambdaClient, MockLambdaClientInfo>("mock").get()!!
            coEvery { client.getFunctionConfiguration(any<GetFunctionConfigurationRequest>()) } throws LambdaException("not found")

            val provider = project.providers.of(GetFunctionConfigurationValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.functionName.set("missing")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
