package com.kelvsyc.gradle.aws.java.secretsmanager

import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.java.secretsmanager.MockSecretsManagerClientInfoInternal
import com.kelvsyc.gradle.plugins.SecretsManagerJavaBasePlugin
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
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException

class SecretsManagerValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret string on success") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SecretsManagerJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretsManagerClientInfo::class, MockSecretsManagerClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretsManagerClientInfo>("mock") {}
            val slot = slot<GetSecretValueRequest>()
            val client = extension.getClient<SecretsManagerClient, _>("mock").get()
            val response = mockk<GetSecretValueResponse>()
            every { response.secretString() } returns "super-secret"
            every { client.getSecretValue(capture(slot)) } returns response

            val provider = project.providers.of(SecretsManagerValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.secretName.set("my-secret")
            }
            val result = provider.get()

            result shouldBe "super-secret"
            slot.captured.secretId() shouldBe "my-secret"
        }

        test("obtain - returns null when SecretsManagerException is thrown") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SecretsManagerJavaBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretsManagerClientInfo::class, MockSecretsManagerClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretsManagerClientInfo>("mock") {}
            val client = extension.getClient<SecretsManagerClient, _>("mock").get()
            every { client.getSecretValue(any<GetSecretValueRequest>()) } throws SecretsManagerException.builder().message("not found").build()

            val provider = project.providers.of(SecretsManagerValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.secretName.set("missing-secret")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}

