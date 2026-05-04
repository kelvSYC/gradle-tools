package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.ListVersionsRequest
import com.google.devtools.artifactregistry.v1.Version
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

class ListVersionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns version resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            project.pluginManager.apply(GoogleCloudArtifactRegistryBasePlugin::class)
            val extension = project.the<ClientsBaseExtension>()
            extension.service.get()
                .registerBinding(MockArtifactRegistryClientInfo::class, MockArtifactRegistryClientInfoInternal::class)
            extension.service.get().registerIfAbsent<MockArtifactRegistryClientInfo>("mock") {}

            val client = extension.getClient<ArtifactRegistryClient, MockArtifactRegistryClientInfo>("mock").get()!!
            val versions = listOf(
                Version.newBuilder().setName("projects/p/locations/us-east1/repositories/r/packages/pkg/versions/1.0.0").build(),
                Version.newBuilder().setName("projects/p/locations/us-east1/repositories/r/packages/pkg/versions/2.0.0").build(),
            )
            val paged = mockk<ArtifactRegistryClient.ListVersionsPagedResponse>()
            every { paged.iterateAll() } returns versions

            val slot = slot<ListVersionsRequest>()
            every { client.listVersions(capture(slot)) } returns paged

            val provider = project.providers.of(ListVersionsValueSource::class) {
                parameters.service.set(extension.service)
                parameters.clientName.set("mock")
                parameters.projectName.set("p")
                parameters.location.set("us-east1")
                parameters.repository.set("r")
                parameters.packageName.set("pkg")
            }

            provider.get() shouldBe listOf(
                "projects/p/locations/us-east1/repositories/r/packages/pkg/versions/1.0.0",
                "projects/p/locations/us-east1/repositories/r/packages/pkg/versions/2.0.0",
            )
            slot.captured.parent shouldBe "projects/p/locations/us-east1/repositories/r/packages/pkg"
        }
    }
}
