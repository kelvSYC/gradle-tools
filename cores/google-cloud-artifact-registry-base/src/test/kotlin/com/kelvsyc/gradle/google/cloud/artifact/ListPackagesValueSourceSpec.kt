package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.ListPackagesRequest
import com.google.devtools.artifactregistry.v1.Package
import com.kelvsyc.gradle.clients.ClientsBaseExtension
import com.kelvsyc.gradle.internal.google.cloud.artifact.MockArtifactRegistryClientInfoInternal
import com.kelvsyc.gradle.plugins.GoogleCloudArtifactRegistryBasePlugin
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.apply
import org.gradle.kotlin.dsl.of
import org.gradle.kotlin.dsl.the
import org.gradle.testfixtures.ProjectBuilder

class ListPackagesValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns package resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudArtifactRegistryBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get()
                .registerBinding(MockArtifactRegistryClientInfo::class, MockArtifactRegistryClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockArtifactRegistryClientInfo>("mock") {}

            val client = extension.getClient<ArtifactRegistryClient, MockArtifactRegistryClientInfo>("mock").get()!!
            val packages = listOf(
                Package.newBuilder().setName("projects/p/locations/us-east1/repositories/r/packages/a").build(),
                Package.newBuilder().setName("projects/p/locations/us-east1/repositories/r/packages/b").build(),
            )
            val paged = mockk<ArtifactRegistryClient.ListPackagesPagedResponse>()
            every { paged.iterateAll() } returns packages

            val slot = slot<ListPackagesRequest>()
            every { client.listPackages(capture(slot)) } returns paged

            val provider = project.providers.of(ListPackagesValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.projectName.set("p")
                parameters.location.set("us-east1")
                parameters.repository.set("r")
            }

            provider.get() shouldBe listOf(
                "projects/p/locations/us-east1/repositories/r/packages/a",
                "projects/p/locations/us-east1/repositories/r/packages/b",
            )
            slot.captured.parent shouldBe "projects/p/locations/us-east1/repositories/r"
        }
    }
}
