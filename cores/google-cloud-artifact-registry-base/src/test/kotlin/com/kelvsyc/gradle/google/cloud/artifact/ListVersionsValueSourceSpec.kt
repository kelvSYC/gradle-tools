package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.ListVersionsRequest
import com.google.devtools.artifactregistry.v1.Version
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListVersionsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns version resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ArtifactRegistryClient>()
            MockArtifactRegistryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ar", MockArtifactRegistryClientBuildService::class)

            val versions = listOf(
                Version.newBuilder().setName("projects/p/locations/us-east1/repositories/r/packages/pkg/versions/1.0.0").build(),
                Version.newBuilder().setName("projects/p/locations/us-east1/repositories/r/packages/pkg/versions/2.0.0").build(),
            )
            val paged = mockk<ArtifactRegistryClient.ListVersionsPagedResponse>()
            every { paged.iterateAll() } returns versions

            val slot = slot<ListVersionsRequest>()
            every { client.listVersions(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListVersionsValueSource::class) {
                parameters.service.set(service)
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
