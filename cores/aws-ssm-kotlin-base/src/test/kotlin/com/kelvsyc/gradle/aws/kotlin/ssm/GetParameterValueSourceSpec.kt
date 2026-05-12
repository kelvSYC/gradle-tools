package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.model.GetParameterRequest
import aws.sdk.kotlin.services.ssm.model.GetParameterResponse
import aws.sdk.kotlin.services.ssm.model.Parameter
import aws.sdk.kotlin.services.ssm.model.SsmException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetParameterValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns parameter value on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SsmClient>()
            MockSsmClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ssm", MockSsmClientBuildService::class)
            val slot = slot<GetParameterRequest>()
            coEvery { client.getParameter(capture(slot)) } returns GetParameterResponse {
                parameter = Parameter {
                    name = "/my/parameter"
                    value = "param-value"
                }
            }

            val provider = project.providers.ofKt(GetParameterValueSource::class) {
                parameters.service.set(service)
                parameters.parameterName.set("/my/parameter")
                parameters.withDecryption.set(true)
            }
            val result = provider.get()

            result shouldBe "param-value"
            slot.captured.name shouldBe "/my/parameter"
            slot.captured.withDecryption shouldBe true
        }

        test("obtain - returns null when SsmException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SsmClient>()
            MockSsmClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ssm", MockSsmClientBuildService::class)
            coEvery { client.getParameter(any<GetParameterRequest>()) } throws SsmException("not found")

            val provider = project.providers.ofKt(GetParameterValueSource::class) {
                parameters.service.set(service)
                parameters.parameterName.set("/missing/parameter")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
