package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import aws.sdk.kotlin.services.sts.model.DecodeAuthorizationMessageRequest
import aws.sdk.kotlin.services.sts.model.DecodeAuthorizationMessageResponse
import aws.sdk.kotlin.services.sts.model.StsException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DecodeAuthorizationMessageValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns decoded message on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<StsClient>()
            MockStsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sts", MockStsClientBuildService::class)
            val slot = slot<DecodeAuthorizationMessageRequest>()
            coEvery { client.decodeAuthorizationMessage(capture(slot)) } returns DecodeAuthorizationMessageResponse {
                decodedMessage = "{\"allowed\":false}"
            }

            val provider = project.providers.ofKt(DecodeAuthorizationMessageValueSource::class) {
                parameters.service.set(service)
                parameters.encodedMessage.set("encoded-blob")
            }
            val result = provider.get()

            result shouldBe "{\"allowed\":false}"
            slot.captured.encodedMessage shouldBe "encoded-blob"
        }

        test("obtain - returns null when StsException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<StsClient>()
            MockStsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sts", MockStsClientBuildService::class)
            coEvery {
                client.decodeAuthorizationMessage(any<DecodeAuthorizationMessageRequest>())
            } throws StsException("expired")

            val provider = project.providers.ofKt(DecodeAuthorizationMessageValueSource::class) {
                parameters.service.set(service)
                parameters.encodedMessage.set("encoded-blob")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
