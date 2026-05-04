package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.GetRepositoryRequest
import com.google.devtools.artifactregistry.v1.Repository
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.google.cloud.artifact.MockArtifactRegistryClientInfoInternal
import com.kelvsyc.gradle.plugins.GoogleCloudArtifactRegistryBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class GetRepositoryValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns Repository for the requested resource name") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudArtifactRegistryBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get()
                .registerBinding(MockArtifactRegistryClientInfo::class, MockArtifactRegistryClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockArtifactRegistryClientInfo>("mock") {}

            val client = extension.getClient<ArtifactRegistryClient, MockArtifactRegistryClientInfo>("mock").get()!!
            val expected = Repository.newBuilder().apply {
                name = "projects/my-project/locations/us-east1/repositories/my-repo"
            }.build()
            val slot = slot<GetRepositoryRequest>()
            every { client.getRepository(capture(slot)) } returns expected

            val provider = project.providers.of(GetRepositoryValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.projectName.set("my-project")
                parameters.location.set("us-east1")
                parameters.repository.set("my-repo")
            }

            provider.get() shouldBe expected
            slot.captured.name shouldBe "projects/my-project/locations/us-east1/repositories/my-repo"
        }
    }
}
