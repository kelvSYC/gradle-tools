package com.kelvsyc.gradle.aws.java.sts

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.sts.MockStsClientInfoInternal
import com.kelvsyc.gradle.plugins.StsJavaBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.DecodeAuthorizationMessageRequest
import software.amazon.awssdk.services.sts.model.DecodeAuthorizationMessageResponse
import software.amazon.awssdk.services.sts.model.StsException

class DecodeAuthorizationMessageValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns decoded message on success") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(StsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockStsClientInfo::class, MockStsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockStsClientInfo>("mock") {}
            val slot = slot<DecodeAuthorizationMessageRequest>()
            val client = extension.getClient<StsClient, _>("mock").get()
            val response = mockk<DecodeAuthorizationMessageResponse>()
            every { response.decodedMessage() } returns "{\"allowed\":false}"
            every { client.decodeAuthorizationMessage(capture(slot)) } returns response

            val provider = project.providers.of(DecodeAuthorizationMessageValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.encodedMessage.set("encoded-blob")
            }
            val result = provider.get()

            result shouldBe "{\"allowed\":false}"
            slot.captured.encodedMessage() shouldBe "encoded-blob"
        }

        test("obtain - returns null when StsException is thrown") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(StsJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockStsClientInfo::class, MockStsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockStsClientInfo>("mock") {}
            val client = extension.getClient<StsClient, _>("mock").get()
            every { client.decodeAuthorizationMessage(any<DecodeAuthorizationMessageRequest>()) } throws StsException.builder().message("expired").build()

            val provider = project.providers.of(DecodeAuthorizationMessageValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.encodedMessage.set("encoded-blob")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
