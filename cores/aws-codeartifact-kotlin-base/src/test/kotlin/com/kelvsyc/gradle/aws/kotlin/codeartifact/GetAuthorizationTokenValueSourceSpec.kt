package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetAuthorizationTokenRequest
import aws.sdk.kotlin.services.codeartifact.model.GetAuthorizationTokenResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

@Suppress("DEPRECATION")
class GetAuthorizationTokenValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns authorization token") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("ca", MockCodeArtifactClientBuildService::class)
            val slot = slot<GetAuthorizationTokenRequest>()
            val response = mockk<GetAuthorizationTokenResponse>()
            coEvery { response.authorizationToken } returns "token-value"
            coEvery { client.getAuthorizationToken(capture(slot)) } returns response

            val provider = project.providers.ofKt(GetAuthorizationTokenValueSource::class) {
                parameters.service.set(service)
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.duration.set(900L)
            }

            provider.get() shouldBe "token-value"
            slot.captured.domain shouldBe "my-domain"
            slot.captured.domainOwner shouldBe "123456789012"
            slot.captured.durationSeconds shouldBe 900L
        }

        test("obtain - returns null when authorizationToken is null") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<CodeartifactClient>()
            MockCodeArtifactClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("ca", MockCodeArtifactClientBuildService::class)
            val response = mockk<GetAuthorizationTokenResponse>()
            coEvery { response.authorizationToken } returns null
            coEvery { client.getAuthorizationToken(any()) } returns response

            val provider = project.providers.ofKt(GetAuthorizationTokenValueSource::class) {
                parameters.service.set(service)
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.duration.set(900L)
            }

            provider.orNull.shouldBeNull()
        }
    }
}
