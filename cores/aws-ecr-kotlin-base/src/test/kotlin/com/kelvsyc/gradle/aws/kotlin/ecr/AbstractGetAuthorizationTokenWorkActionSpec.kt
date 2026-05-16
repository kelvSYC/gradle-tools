package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.ecr.model.AuthorizationData
import aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenRequest
import aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AbstractGetAuthorizationTokenWorkActionSpec : FunSpec() {
    init {
        test("execute - retrieves token and passes it to doExecute") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<EcrClient>()
            MockEcrClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ecr", MockEcrClientBuildService::class)

            val authData = mockk<AuthorizationData>()
            every { authData.authorizationToken } returns "QVdTOnRva2VuLXZhbHVl"

            val response = mockk<GetAuthorizationTokenResponse>()
            every { response.authorizationData } returns listOf(authData)

            coEvery { client.getAuthorizationToken(any<GetAuthorizationTokenRequest>()) } returns response

            val params = project.objects.newInstance<AbstractGetAuthorizationTokenWorkAction.Parameters>()
            params.service.set(service)

            var capturedToken: String? = null
            val action = object : AbstractGetAuthorizationTokenWorkAction() {
                override fun getParameters() = params
                override fun doExecute(token: String) {
                    capturedToken = token
                }
            }
            action.execute()

            capturedToken shouldBe "QVdTOnRva2VuLXZhbHVl"
        }
    }
}
