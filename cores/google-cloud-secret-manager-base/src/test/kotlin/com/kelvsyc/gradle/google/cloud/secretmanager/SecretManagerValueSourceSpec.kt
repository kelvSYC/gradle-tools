package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.api.gax.rpc.ApiException
import com.google.cloud.secretmanager.v1.AccessSecretVersionRequest
import com.google.cloud.secretmanager.v1.AccessSecretVersionResponse
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretPayload
import com.google.cloud.secretmanager.v1.SecretVersionName
import com.google.protobuf.ByteString
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SecretManagerValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret string on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretManagerServiceClient>()
            MockSecretManagerServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sm", MockSecretManagerServiceClientBuildService::class)

            val slot = slot<AccessSecretVersionRequest>()
            val response = mockk<AccessSecretVersionResponse>()
            val payload = mockk<SecretPayload>()
            every { response.payload } returns payload
            every { payload.data } returns ByteString.copyFromUtf8("super-secret")
            every { client.accessSecretVersion(capture(slot)) } returns response

            @Suppress("DEPRECATION")
            val provider = project.providers.ofKt(SecretManagerValueSource::class) {
                parameters.service.set(service)
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
            val client = mockk<SecretManagerServiceClient>()
            MockSecretManagerServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sm", MockSecretManagerServiceClientBuildService::class)

            val slot = slot<AccessSecretVersionRequest>()
            val response = mockk<AccessSecretVersionResponse>()
            val payload = mockk<SecretPayload>()
            every { response.payload } returns payload
            every { payload.data } returns ByteString.copyFromUtf8("secret-value")
            every { client.accessSecretVersion(capture(slot)) } returns response

            @Suppress("DEPRECATION")
            val provider = project.providers.ofKt(SecretManagerValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("my-project")
                parameters.secretId.set("my-secret")
            }
            provider.get()

            slot.captured.name shouldBe SecretVersionName.of("my-project", "my-secret", "latest").toString()
        }

        test("obtain - returns null when ApiException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretManagerServiceClient>()
            MockSecretManagerServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sm", MockSecretManagerServiceClientBuildService::class)

            every { client.accessSecretVersion(any<AccessSecretVersionRequest>()) } throws mockk<ApiException>(relaxed = true)

            @Suppress("DEPRECATION")
            val provider = project.providers.ofKt(SecretManagerValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("my-project")
                parameters.secretId.set("missing-secret")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
