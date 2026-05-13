package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.cloud.secretmanager.v1.ListSecretsRequest
import com.google.cloud.secretmanager.v1.Secret
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListSecretsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<SecretManagerServiceClient>()
            MockSecretManagerServiceClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("sm", MockSecretManagerServiceClientBuildService::class)

            val secrets = listOf(
                Secret.newBuilder().setName("projects/my-project/secrets/alpha").build(),
                Secret.newBuilder().setName("projects/my-project/secrets/bravo").build(),
            )
            val paged = mockk<SecretManagerServiceClient.ListSecretsPagedResponse>()
            every { paged.iterateAll() } returns secrets

            val slot = slot<ListSecretsRequest>()
            every { client.listSecrets(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListSecretsValueSource::class) {
                parameters.service.set(service)
                parameters.projectId.set("my-project")
            }

            provider.get() shouldBe listOf(
                "projects/my-project/secrets/alpha",
                "projects/my-project/secrets/bravo",
            )
            slot.captured.parent shouldBe "projects/my-project"
        }
    }
}
