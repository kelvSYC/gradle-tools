package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetAuthorizationTokenRequest
import aws.sdk.kotlin.services.codeartifact.model.GetAuthorizationTokenResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class AbstractGetAuthorizationTokenWorkActionSpec : FunSpec() {
    init {
        test("execute - retrieves and passes token to doExecute") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client

            val service = project.gradle.sharedServices.registerIfAbsent(
                "ca",
                MockCodeArtifactClientBuildService::class
            )

            val requestSlot = slot<GetAuthorizationTokenRequest>()
            val response = mockk<GetAuthorizationTokenResponse>()
            coEvery { response.authorizationToken } returns "my-token"
            coEvery { client.getAuthorizationToken(capture(requestSlot)) } returns response

            var capturedToken: String? = null
            val action = object : AbstractGetAuthorizationTokenWorkAction() {
                override fun getParameters(): Parameters = run {
                    val params = project.objects.newInstance<Parameters>()
                    params.service.set(service)
                    params.domain.set("my-domain")
                    params.domainOwner.set("123456789012")
                    params.duration.set(3600L)
                    params
                }

                override fun doExecute(token: String) {
                    capturedToken = token
                }
            }
            action.execute()

            capturedToken shouldBe "my-token"
            requestSlot.captured.domain shouldBe "my-domain"
            requestSlot.captured.domainOwner shouldBe "123456789012"
            requestSlot.captured.durationSeconds shouldBe 3600L
        }

        test("execute - throws when authorizationToken is null") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client

            val service = project.gradle.sharedServices.registerIfAbsent(
                "ca",
                MockCodeArtifactClientBuildService::class
            )

            val response = mockk<GetAuthorizationTokenResponse>()
            coEvery { response.authorizationToken } returns null
            coEvery { client.getAuthorizationToken(any()) } returns response

            val action = object : AbstractGetAuthorizationTokenWorkAction() {
                override fun getParameters(): Parameters = run {
                    val params = project.objects.newInstance<Parameters>()
                    params.service.set(service)
                    params.domain.set("my-domain")
                    params.domainOwner.set("123456789012")
                    params.duration.set(3600L)
                    params
                }

                override fun doExecute(token: String) {
                    /* test hook, do nothing */
                }
            }

            shouldThrow<IllegalStateException> {
                action.execute()
            }
        }
    }
}
