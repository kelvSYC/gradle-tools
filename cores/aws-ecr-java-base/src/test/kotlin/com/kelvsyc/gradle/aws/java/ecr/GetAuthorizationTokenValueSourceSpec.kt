package com.kelvsyc.gradle.aws.java.ecr

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.ecr.EcrClient
import software.amazon.awssdk.services.ecr.model.AuthorizationData
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenRequest
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenResponse

class GetAuthorizationTokenValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns first authorization token") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<EcrClient>()
            MockEcrClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ecr", MockEcrClientBuildService::class)

            val authData = mockk<AuthorizationData>()
            every { authData.authorizationToken() } returns "QVdTOnRva2VuLXZhbHVl"

            val response = mockk<GetAuthorizationTokenResponse>()
            every { response.authorizationData() } returns listOf(authData)

            every { client.getAuthorizationToken(any<GetAuthorizationTokenRequest>()) } returns response

            @Suppress("DEPRECATION")
            val provider = project.providers.ofKt(GetAuthorizationTokenValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result shouldBe "QVdTOnRva2VuLXZhbHVl"
        }
    }
}
