package com.kelvsyc.gradle.google.cloud.secretmanager

import com.google.cloud.secretmanager.v1.ListSecretsRequest
import com.google.cloud.secretmanager.v1.Secret
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.google.cloud.secretmanager.MockSecretManagerClientInfoInternal
import com.kelvsyc.gradle.plugins.GoogleCloudSecretManagerBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class ListSecretsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns secret resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudSecretManagerBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get().registerBinding(MockSecretManagerClientInfo::class, MockSecretManagerClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockSecretManagerClientInfo>("mock") {}

            val client = extension.getClient<SecretManagerServiceClient, MockSecretManagerClientInfo>("mock").get()!!
            val secrets = listOf(
                Secret.newBuilder().setName("projects/my-project/secrets/alpha").build(),
                Secret.newBuilder().setName("projects/my-project/secrets/bravo").build(),
            )
            val paged = mockk<SecretManagerServiceClient.ListSecretsPagedResponse>()
            every { paged.iterateAll() } returns secrets

            val slot = slot<ListSecretsRequest>()
            every { client.listSecrets(capture(slot)) } returns paged

            val provider = project.providers.of(ListSecretsValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
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
