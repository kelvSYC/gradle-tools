package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.model.GetSecretValueRequest
import aws.sdk.kotlin.services.secretsmanager.model.GetSecretValueResponse
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.aws.kotlin.secretsmanager.MockSecretsManagerClientInfoInternal
import com.kelvsyc.gradle.plugins.SecretsManagerKotlinBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class SecretsManagerValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret string on success") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(SecretsManagerKotlinBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretsManagerClientInfo::class, MockSecretsManagerClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretsManagerClientInfo>("mock") {}
            val slot = slot<GetSecretValueRequest>()
            val client = extension.getClient<SecretsManagerClient, MockSecretsManagerClientInfo>("mock").get()!!
            coEvery { client.getSecretValue(capture(slot)) } returns GetSecretValueResponse {
                secretString = "super-secret"
            }

            val provider = project.providers.of(SecretsManagerValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.secretName.set("my-secret")
            }
            val result = provider.get()

            result shouldBe "super-secret"
            slot.captured.secretId shouldBe "my-secret"
        }
    }
}
