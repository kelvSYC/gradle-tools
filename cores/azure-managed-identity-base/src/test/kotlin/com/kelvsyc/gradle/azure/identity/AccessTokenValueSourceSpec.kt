package com.kelvsyc.gradle.azure.identity

import com.azure.core.credential.AccessToken
import com.azure.core.credential.TokenRequestContext
import com.azure.identity.ManagedIdentityCredential
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

class AccessTokenValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns token string for requested scopes") {
            val project = ProjectBuilder.builder().build()
            val credential = mockk<ManagedIdentityCredential>()
            MockManagedIdentityCredentialBuildService.mockClient = credential
            val service = project.gradle.sharedServices
                .registerIfAbsent("mi", MockManagedIdentityCredentialBuildService::class)

            val contextSlot = slot<TokenRequestContext>()
            val accessToken = AccessToken("my-token-value", OffsetDateTime.now().plusHours(1))
            every { credential.getToken(capture(contextSlot)) } returns Mono.just(accessToken)

            val provider = project.providers.ofKt(AccessTokenValueSource::class) {
                parameters.service.set(service)
                parameters.scopes.add("https://management.azure.com/.default")
            }
            val result = provider.get()

            result shouldBe "my-token-value"
            contextSlot.captured.scopes shouldBe listOf("https://management.azure.com/.default")
        }
    }
}
