package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import aws.sdk.kotlin.services.ssm.model.GetParameterRequest
import aws.sdk.kotlin.services.ssm.model.GetParameterResponse
import aws.sdk.kotlin.services.ssm.model.Parameter
import aws.sdk.kotlin.services.ssm.model.SsmException
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.ssm.MockSsmClientInfoInternal
import com.kelvsyc.gradle.plugins.SsmKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class GetParameterValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns parameter value on success") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SsmKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSsmClientInfo::class, MockSsmClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSsmClientInfo>("mock") {}
            val slot = slot<GetParameterRequest>()
            val client = extension.getClient<SsmClient, MockSsmClientInfo>("mock").get()!!
            coEvery { client.getParameter(capture(slot)) } returns GetParameterResponse {
                parameter = Parameter {
                    name = "/my/parameter"
                    value = "param-value"
                }
            }

            val provider = project.providers.of(GetParameterValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
            project.pluginManager.apply(SsmKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSsmClientInfo::class, MockSsmClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSsmClientInfo>("mock") {}
            val client = extension.getClient<SsmClient, MockSsmClientInfo>("mock").get()!!
            coEvery { client.getParameter(any<GetParameterRequest>()) } throws SsmException("not found")

            val provider = project.providers.of(GetParameterValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.parameterName.set("/missing/parameter")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
