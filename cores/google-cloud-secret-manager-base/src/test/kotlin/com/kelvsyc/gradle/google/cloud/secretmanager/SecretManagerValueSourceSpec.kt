package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.api.gax.rpc.ApiException
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretPayload
import com.google.cloud.secretmanager.v1.SecretVersionName
import com.google.protobuf.ByteString
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.google.cloud.secretmanager.MockSecretManagerClientInfoInternal
import com.kelvsyc.gradle.plugins.GoogleCloudSecretManagerBasePlugin
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

class SecretManagerValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret string on success") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudSecretManagerBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretManagerClientInfo::class, MockSecretManagerClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretManagerClientInfo>("mock") {}
            val slot = slot<AccessSecretVersionRequest>()
            val client = extension.getClient<SecretManagerServiceClient, MockSecretManagerClientInfo>("mock").get()!!
            val response = mockk<AccessSecretVersionResponse>()
            val payload = mockk<SecretPayload>()
            every { response.payload } returns payload
            every { payload.data } returns ByteString.copyFromUtf8("super-secret")
            every { client.accessSecretVersion(capture(slot)) } returns response

            val provider = project.providers.of(SecretManagerValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.projectId.set("my-project")
                parameters.secretId.set("my-secret")
                parameters.versionId.set("1")
            }
            val result = provider.get()

            result shouldBe "super-secret"
            slot.captured.name shouldBe SecretVersionName.of("my-project", "my-secret", "1").toString()
        }

        test("obtain - uses 'latest' version when versionId not set") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudSecretManagerBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretManagerClientInfo::class, MockSecretManagerClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretManagerClientInfo>("mock") {}
            val slot = slot<AccessSecretVersionRequest>()
            val client = extension.getClient<SecretManagerServiceClient, MockSecretManagerClientInfo>("mock").get()!!
            val response = mockk<AccessSecretVersionResponse>()
            val payload = mockk<SecretPayload>()
            every { response.payload } returns payload
            every { payload.data } returns ByteString.copyFromUtf8("secret-value")
            every { client.accessSecretVersion(capture(slot)) } returns response

            val provider = project.providers.of(SecretManagerValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.projectId.set("my-project")
                parameters.secretId.set("my-secret")
            }
            provider.get()

            slot.captured.name shouldBe SecretVersionName.of("my-project", "my-secret", "latest").toString()
        }

        test("obtain - returns null when ApiException is thrown") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudSecretManagerBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretManagerClientInfo::class, MockSecretManagerClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretManagerClientInfo>("mock") {}
            val client = extension.getClient<SecretManagerServiceClient, MockSecretManagerClientInfo>("mock").get()!!
            every { client.accessSecretVersion(any<AccessSecretVersionRequest>()) } throws mockk<ApiException>(relaxed = true)

            val provider = project.providers.of(SecretManagerValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.projectId.set("my-project")
                parameters.secretId.set("missing-secret")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
