package com.kelvsyc.gradle.aws.java.codeartifact

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenResponse

class AbstractGetAuthorizationTokenWorkActionSpec : FunSpec() {
    init {
        test("execute - passes domain, domainOwner, duration to request and calls doExecute with token") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("codeartifact", MockCodeArtifactClientBuildService::class)

            val requestSlot = slot<GetAuthorizationTokenRequest>()
            val response = mockk<GetAuthorizationTokenResponse>()
            every { response.authorizationToken() } returns "my-token"
            every { client.getAuthorizationToken(capture(requestSlot)) } returns response

            val params = project.objects.newInstance<AbstractGetAuthorizationTokenWorkAction.Parameters>()
            params.service.set(service)
            params.domain.set("my-domain")
            params.domainOwner.set("123456789012")
            params.duration.set(3600L)

            var capturedToken: String? = null
            val action = object : AbstractGetAuthorizationTokenWorkAction() {
                override fun getParameters() = params
                override fun doExecute(token: String) {
                    capturedToken = token
                }
            }
            action.execute()

            val captured = requestSlot.captured
            captured.domain() shouldBe "my-domain"
            captured.domainOwner() shouldBe "123456789012"
            captured.durationSeconds() shouldBe 3600L
            capturedToken shouldBe "my-token"
        }
    }
}
