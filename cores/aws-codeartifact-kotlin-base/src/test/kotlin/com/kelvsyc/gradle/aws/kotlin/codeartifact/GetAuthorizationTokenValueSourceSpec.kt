package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetAuthorizationTokenRequest
import aws.sdk.kotlin.services.codeartifact.model.GetAuthorizationTokenResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.codeartifact.MockCodeArtifactClientInfoInternal
import com.kelvsyc.gradle.plugins.CodeArtifactKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class GetAuthorizationTokenValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns authorization token") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(CodeArtifactKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockCodeArtifactClientInfo::class, MockCodeArtifactClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockCodeArtifactClientInfo>("mock") {}

            val client = extension.getClient<CodeartifactClient, MockCodeArtifactClientInfo>("mock").get()!!
            val slot = slot<GetAuthorizationTokenRequest>()
            val response = mockk<GetAuthorizationTokenResponse>()
            coEvery { response.authorizationToken } returns "token-value"
            coEvery { client.getAuthorizationToken(capture(slot)) } returns response

            val provider = project.providers.of(GetAuthorizationTokenValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
            project.pluginManager.apply(CodeArtifactKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockCodeArtifactClientInfo::class, MockCodeArtifactClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockCodeArtifactClientInfo>("mock") {}

            val client = extension.getClient<CodeartifactClient, MockCodeArtifactClientInfo>("mock").get()!!
            val response = mockk<GetAuthorizationTokenResponse>()
            coEvery { response.authorizationToken } returns null
            coEvery { client.getAuthorizationToken(any()) } returns response

            val provider = project.providers.of(GetAuthorizationTokenValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.domain.set("my-domain")
                parameters.domainOwner.set("123456789012")
                parameters.duration.set(900L)
            }

            provider.orNull.shouldBeNull()
        }
    }
}
