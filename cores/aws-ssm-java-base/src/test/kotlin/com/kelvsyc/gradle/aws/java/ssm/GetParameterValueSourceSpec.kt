package com.kelvsyc.gradle.aws.java.ssm

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.ssm.SsmClient
import software.amazon.awssdk.services.ssm.model.GetParameterRequest
import software.amazon.awssdk.services.ssm.model.GetParameterResponse
import software.amazon.awssdk.services.ssm.model.Parameter
import software.amazon.awssdk.services.ssm.model.SsmException

class GetParameterValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns parameter value on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SsmClient>()
            MockSsmClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ssm", MockSsmClientBuildService::class)
            val slot = slot<GetParameterRequest>()
            val parameter = mockk<Parameter>()
            every { parameter.value() } returns "param-value"
            val response = mockk<GetParameterResponse>()
            every { response.parameter() } returns parameter
            every { client.getParameter(capture(slot)) } returns response

            val provider = project.providers.ofKt(GetParameterValueSource::class) {
                parameters.service.set(service)
                parameters.parameterName.set("/my/parameter")
                parameters.withDecryption.set(true)
            }
            val result = provider.get()

            result shouldBe "param-value"
            slot.captured.name() shouldBe "/my/parameter"
            slot.captured.withDecryption() shouldBe true
        }

        test("obtain - returns null when SsmException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SsmClient>()
            MockSsmClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ssm", MockSsmClientBuildService::class)
            every {
                client.getParameter(any<GetParameterRequest>())
            } throws SsmException.builder().message("not found").build()

            val provider = project.providers.ofKt(GetParameterValueSource::class) {
                parameters.service.set(service)
                parameters.parameterName.set("/missing/parameter")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
