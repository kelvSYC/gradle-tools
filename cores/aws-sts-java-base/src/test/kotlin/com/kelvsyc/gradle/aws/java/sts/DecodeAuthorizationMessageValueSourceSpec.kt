package com.kelvsyc.gradle.aws.java.sts

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.DecodeAuthorizationMessageRequest
import software.amazon.awssdk.services.sts.model.DecodeAuthorizationMessageResponse
import software.amazon.awssdk.services.sts.model.StsException

class DecodeAuthorizationMessageValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns decoded message on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<StsClient>()
            MockStsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sts", MockStsClientBuildService::class)
            val slot = slot<DecodeAuthorizationMessageRequest>()
            val response = mockk<DecodeAuthorizationMessageResponse>()
            every { response.decodedMessage() } returns "{\"allowed\":false}"
            every { client.decodeAuthorizationMessage(capture(slot)) } returns response

            val provider = project.providers.ofKt(DecodeAuthorizationMessageValueSource::class) {
                parameters.service.set(service)
                parameters.encodedMessage.set("encoded-blob")
            }
            val result = provider.get()

            result shouldBe "{\"allowed\":false}"
            slot.captured.encodedMessage() shouldBe "encoded-blob"
        }

        test("obtain - returns null when StsException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<StsClient>()
            MockStsClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sts", MockStsClientBuildService::class)
            every {
                client.decodeAuthorizationMessage(any<DecodeAuthorizationMessageRequest>())
            } throws StsException.builder().message("expired").build()

            val provider = project.providers.ofKt(DecodeAuthorizationMessageValueSource::class) {
                parameters.service.set(service)
                parameters.encodedMessage.set("encoded-blob")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
