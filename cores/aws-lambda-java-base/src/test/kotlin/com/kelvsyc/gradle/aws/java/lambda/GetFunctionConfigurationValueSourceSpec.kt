package com.kelvsyc.gradle.aws.java.lambda

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.lambda.MockLambdaClientInfoInternal
import com.kelvsyc.gradle.plugins.LambdaJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.lambda.LambdaClient
import software.amazon.awssdk.services.lambda.model.GetFunctionConfigurationRequest
import software.amazon.awssdk.services.lambda.model.GetFunctionConfigurationResponse
import software.amazon.awssdk.services.lambda.model.LambdaException

class GetFunctionConfigurationValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns function ARN on success") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(LambdaJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockLambdaClientInfo::class, MockLambdaClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockLambdaClientInfo>("mock") {}
            val slot = slot<GetFunctionConfigurationRequest>()
            val client = extension.getClient<LambdaClient, _>("mock").get()
            val response = mockk<GetFunctionConfigurationResponse>()
            every { response.functionArn() } returns "arn:aws:lambda:us-east-1:123:function:my-fn:42"
            every { client.getFunctionConfiguration(capture(slot)) } returns response

            val provider = project.providers.of(GetFunctionConfigurationValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
            project.pluginManager.apply(LambdaJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockLambdaClientInfo::class, MockLambdaClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockLambdaClientInfo>("mock") {}
            val client = extension.getClient<LambdaClient, _>("mock").get()
            every { client.getFunctionConfiguration(any<GetFunctionConfigurationRequest>()) } throws LambdaException.builder().message("not found").build()

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
