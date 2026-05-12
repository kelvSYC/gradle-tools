package com.kelvsyc.gradle.azure.keyvault

import com.azure.core.exception.HttpResponseException
import com.azure.core.http.HttpResponse
import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.models.KeyVaultSecret
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.azure.keyvault.MockSecretClientInfoInternal
import com.kelvsyc.gradle.plugins.AzureKeyVaultBasePlugin
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

class KeyVaultSecretValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret value on success") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(AzureKeyVaultBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretClientInfo::class, MockSecretClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretClientInfo>("mock") {}
            val nameSlot = slot<String>()
            val client = extension.getClient<SecretClient, MockSecretClientInfo>("mock").get()!!
            val secret = mockk<KeyVaultSecret>()
            every { secret.value } returns "super-secret"
            every { client.getSecret(capture(nameSlot)) } returns secret

            val provider = project.providers.of(KeyVaultSecretValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.secretName.set("my-secret")
            }
            val result = provider.get()

            result shouldBe "super-secret"
            nameSlot.captured shouldBe "my-secret"
        }

        test("obtain - passes version when set") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(AzureKeyVaultBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretClientInfo::class, MockSecretClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretClientInfo>("mock") {}
            val nameSlot = slot<String>()
            val versionSlot = slot<String>()
            val client = extension.getClient<SecretClient, MockSecretClientInfo>("mock").get()!!
            val secret = mockk<KeyVaultSecret>()
            every { secret.value } returns "versioned-secret"
            every { client.getSecret(capture(nameSlot), capture(versionSlot)) } returns secret

            val provider = project.providers.of(KeyVaultSecretValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
            project.pluginManager.apply(AzureKeyVaultBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretClientInfo::class, MockSecretClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretClientInfo>("mock") {}
            val client = extension.getClient<SecretClient, MockSecretClientInfo>("mock").get()!!
            every { client.getSecret(any<String>()) } throws HttpResponseException("not found", mockk<HttpResponse>(relaxed = true))

            val provider = project.providers.of(KeyVaultSecretValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.secretName.set("missing-secret")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
