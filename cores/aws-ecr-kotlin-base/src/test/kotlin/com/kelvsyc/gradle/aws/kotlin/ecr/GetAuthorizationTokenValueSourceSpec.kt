package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.ecr.model.AuthorizationData
import aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenRequest
import aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.ecr.MockEcrClientInfoInternal
import com.kelvsyc.gradle.plugins.EcrKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class GetAuthorizationTokenValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns first authorization token") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(EcrKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockEcrClientInfo::class, MockEcrClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockEcrClientInfo>("mock") {}
            val client = extension.getClient<EcrClient, MockEcrClientInfo>("mock").get()!!
            coEvery { client.getAuthorizationToken(any<GetAuthorizationTokenRequest>()) } returns GetAuthorizationTokenResponse {
                authorizationData = listOf(
                    AuthorizationData {
                        authorizationToken = "QVdTOnRva2VuLXZhbHVl"
                        proxyEndpoint = "https://123456789012.dkr.ecr.us-east-1.amazonaws.com"
                    }
                )
            }

            val provider = project.providers.of(GetAuthorizationTokenValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
            }
            val result = provider.get()

            result shouldBe "QVdTOnRva2VuLXZhbHVl"
        }
    }
}
