package com.kelvsyc.gradle.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import aws.sdk.kotlin.services.secretsmanager.model.GetSecretValueRequest
import aws.sdk.kotlin.services.secretsmanager.model.GetSecretValueResponse
import aws.sdk.kotlin.services.secretsmanager.model.SecretsManagerException
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SecretsManagerValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret string on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretsManagerClient>()
            MockSecretsManagerClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("sm", MockSecretsManagerClientBuildService::class)
            val slot = slot<GetSecretValueRequest>()
            coEvery { client.getSecretValue(capture(slot)) } returns GetSecretValueResponse {
                secretString = "super-secret"
            }

            val provider = project.providers.ofKt(SecretsManagerValueSource::class) {
                parameters.service.set(service)
                parameters.secretName.set("my-secret")
            }
            val result = provider.get()

            result shouldBe "super-secret"
            slot.captured.secretId shouldBe "my-secret"
        }

        test("obtain - returns null when SecretsManagerException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretsManagerClient>()
            MockSecretsManagerClientBuildService.mockClient = client
            val service =
                project.gradle.sharedServices.registerIfAbsent("sm", MockSecretsManagerClientBuildService::class)
            coEvery {
                client.getSecretValue(any<GetSecretValueRequest>())
            } throws SecretsManagerException("not found")

            val provider = project.providers.ofKt(SecretsManagerValueSource::class) {
                parameters.service.set(service)
                parameters.secretName.set("missing-secret")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
