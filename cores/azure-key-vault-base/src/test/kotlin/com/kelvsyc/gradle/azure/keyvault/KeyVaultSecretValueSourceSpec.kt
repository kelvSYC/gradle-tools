package com.kelvsyc.gradle.azure.keyvault

import com.azure.core.exception.HttpResponseException
import com.azure.core.http.HttpResponse
import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.models.KeyVaultSecret
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class KeyVaultSecretValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret value on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretClient>()
            MockSecretClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("kv", MockSecretClientBuildService::class)
            val nameSlot = slot<String>()
            val secret = mockk<KeyVaultSecret>()
            every { secret.value } returns "super-secret"
            every { client.getSecret(capture(nameSlot)) } returns secret

            @Suppress("DEPRECATION")
            val provider = project.providers.ofKt(KeyVaultSecretValueSource::class) {
                parameters.service.set(service)
                parameters.secretName.set("my-secret")
            }
            val result = provider.get()

            result shouldBe "super-secret"
            nameSlot.captured shouldBe "my-secret"
        }

        test("obtain - passes version when set") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretClient>()
            MockSecretClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("kv", MockSecretClientBuildService::class)
            val nameSlot = slot<String>()
            val versionSlot = slot<String>()
            val secret = mockk<KeyVaultSecret>()
            every { secret.value } returns "versioned-secret"
            every { client.getSecret(capture(nameSlot), capture(versionSlot)) } returns secret

            @Suppress("DEPRECATION")
            val provider = project.providers.ofKt(KeyVaultSecretValueSource::class) {
                parameters.service.set(service)
                parameters.secretName.set("my-secret")
                parameters.version.set("abc123")
            }
            val result = provider.get()

            result shouldBe "versioned-secret"
            nameSlot.captured shouldBe "my-secret"
            versionSlot.captured shouldBe "abc123"
        }

        test("obtain - returns null when HttpResponseException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretClient>()
            MockSecretClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("kv", MockSecretClientBuildService::class)
            every { client.getSecret(any<String>()) } throws HttpResponseException("not found", mockk<HttpResponse>(relaxed = true))

            @Suppress("DEPRECATION")
            val provider = project.providers.ofKt(KeyVaultSecretValueSource::class) {
                parameters.service.set(service)
                parameters.secretName.set("missing-secret")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
