package com.kelvsyc.gradle.aws.java.secretsmanager

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueRequest
import software.amazon.awssdk.services.secretsmanager.model.GetSecretValueResponse
import software.amazon.awssdk.services.secretsmanager.model.SecretsManagerException

class SecretsManagerValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret string on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretsManagerClient>()
            MockSecretsManagerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "sm",
                MockSecretsManagerClientBuildService::class
            )
            val slot = slot<GetSecretValueRequest>()
            val response = mockk<GetSecretValueResponse>()
            every { response.secretString() } returns "super-secret"
            every { client.getSecretValue(capture(slot)) } returns response

            val provider = project.providers.ofKt(SecretsManagerValueSource::class) {
                parameters.service.set(service)
                parameters.secretName.set("my-secret")
            }
            val result = provider.get()

            result shouldBe "super-secret"
            slot.captured.secretId() shouldBe "my-secret"
        }

        test("obtain - returns null when SecretsManagerException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretsManagerClient>()
            MockSecretsManagerClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "sm",
                MockSecretsManagerClientBuildService::class
            )
            every {
                client.getSecretValue(any<GetSecretValueRequest>())
            } throws SecretsManagerException.builder().message("not found").build()

            val provider = project.providers.ofKt(SecretsManagerValueSource::class) {
                parameters.service.set(service)
                parameters.secretName.set("missing-secret")
            }
            val result = provider.orNull

            result.shouldBeNull()
        }
    }
}
