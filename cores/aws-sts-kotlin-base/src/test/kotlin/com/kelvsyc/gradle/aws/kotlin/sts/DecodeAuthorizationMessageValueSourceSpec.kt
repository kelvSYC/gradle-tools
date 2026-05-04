package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import aws.sdk.kotlin.services.sts.model.DecodeAuthorizationMessageRequest
import aws.sdk.kotlin.services.sts.model.DecodeAuthorizationMessageResponse
import aws.sdk.kotlin.services.sts.model.StsException
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.sts.MockStsClientInfoInternal
import com.kelvsyc.gradle.plugins.StsKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class DecodeAuthorizationMessageValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns decoded message on success") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(StsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockStsClientInfo::class, MockStsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockStsClientInfo>("mock") {}
            val slot = slot<DecodeAuthorizationMessageRequest>()
            val client = extension.getClient<StsClient, MockStsClientInfo>("mock").get()!!
            coEvery { client.decodeAuthorizationMessage(capture(slot)) } returns DecodeAuthorizationMessageResponse {
                decodedMessage = "{\"allowed\":false}"
            }

            val provider = project.providers.of(DecodeAuthorizationMessageValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.encodedMessage.set("encoded-blob")
            }
            val result = provider.get()

            result shouldBe "{\"allowed\":false}"
            slot.captured.encodedMessage shouldBe "encoded-blob"
        }

        test("obtain - returns null when StsException is thrown") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(StsKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockStsClientInfo::class, MockStsClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockStsClientInfo>("mock") {}
            val client = extension.getClient<StsClient, MockStsClientInfo>("mock").get()!!
            coEvery { client.decodeAuthorizationMessage(any<DecodeAuthorizationMessageRequest>()) } throws StsException("expired")

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
