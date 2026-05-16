package com.kelvsyc.gradle.azure.identity

import com.azure.core.credential.AccessToken
import com.azure.core.credential.TokenRequestContext
import com.azure.identity.ManagedIdentityCredential
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import reactor.core.publisher.Mono
import java.time.OffsetDateTime

class AbstractGetAccessTokenWorkActionSpec : FunSpec() {
    init {
        test("execute - retrieves and passes token to doExecute") {
            val project = ProjectBuilder.builder().build()
            val credential = mockk<ManagedIdentityCredential>()
            MockManagedIdentityCredentialBuildService.mockClient = credential
            val service = project.gradle.sharedServices
                .registerIfAbsent("mi", MockManagedIdentityCredentialBuildService::class)

            val contextSlot = slot<TokenRequestContext>()
            val accessToken = AccessToken("my-token-value", OffsetDateTime.now().plusHours(1))
            every { credential.getToken(capture(contextSlot)) } returns Mono.just(accessToken)

            var capturedToken: String? = null
            val action = object : AbstractGetAccessTokenWorkAction() {
                override fun getParameters(): Parameters = run {
                    val params = project.objects.newInstance<Parameters>()
                    params.service.set(service)
                    params.scopes.add("https://management.azure.com/.default")
                    params
                }

                override fun doExecute(token: String) {
                    capturedToken = token
                }
            }
            action.execute()

            capturedToken shouldBe "my-token-value"
            contextSlot.captured.scopes shouldBe listOf("https://management.azure.com/.default")
        }
    }
}
