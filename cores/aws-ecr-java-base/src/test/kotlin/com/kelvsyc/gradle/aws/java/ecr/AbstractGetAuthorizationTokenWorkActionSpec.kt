package com.kelvsyc.gradle.aws.java.ecr

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.ecr.EcrClient
import software.amazon.awssdk.services.ecr.model.AuthorizationData
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenRequest
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenResponse

class AbstractGetAuthorizationTokenWorkActionSpec : FunSpec() {
    init {
        test("execute - retrieves and passes token to doExecute") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<EcrClient>()
            MockEcrClientBuildService.mockClient = client

            val service = project.gradle.sharedServices.registerIfAbsent("ecr", MockEcrClientBuildService::class)

            val expectedToken = "QVdTOnRva2VuLXZhbHVl"
            val authData = mockk<AuthorizationData>()
            every { authData.authorizationToken() } returns expectedToken
            val response = mockk<GetAuthorizationTokenResponse>()
            every { response.authorizationData() } returns listOf(authData)
            every { client.getAuthorizationToken(any<GetAuthorizationTokenRequest>()) } returns response

            var capturedToken: String? = null
            val action = object : AbstractGetAuthorizationTokenWorkAction() {
                override fun getParameters(): Parameters = run {
                    val params = project.objects.newInstance<Parameters>()
                    params.service.set(service)
                    params
                }

                override fun doExecute(token: String) {
                    capturedToken = token
                }
            }
            action.execute()

            capturedToken shouldBe expectedToken
            verify { client.getAuthorizationToken(any<GetAuthorizationTokenRequest>()) }
        }
    }
}
