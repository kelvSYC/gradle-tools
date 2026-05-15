package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.ecr.model.AuthorizationData
import aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenRequest
import aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetAuthorizationTokenValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns first authorization token") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<EcrClient>()
            MockEcrClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ecr", MockEcrClientBuildService::class)
            coEvery {
                client.getAuthorizationToken(any<GetAuthorizationTokenRequest>())
            } returns GetAuthorizationTokenResponse {
                authorizationData = listOf(
                    AuthorizationData {
                        authorizationToken = "QVdTOnRva2VuLXZhbHVl"
                        proxyEndpoint = "https://123456789012.dkr.ecr.us-east-1.amazonaws.com"
                    }
                )
            }

            @Suppress("DEPRECATION")
            val provider = project.providers.ofKt(GetAuthorizationTokenValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result shouldBe "QVdTOnRva2VuLXZhbHVl"
        }
    }
}
