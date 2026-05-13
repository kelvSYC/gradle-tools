package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.ListPackagesRequest
import com.google.devtools.artifactregistry.v1.Package
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListPackagesValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns package resource names from paginated response") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ArtifactRegistryClient>()
            MockArtifactRegistryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ar", MockArtifactRegistryClientBuildService::class)

            val packages = listOf(
                Package.newBuilder().setName("projects/p/locations/us-east1/repositories/r/packages/a").build(),
                Package.newBuilder().setName("projects/p/locations/us-east1/repositories/r/packages/b").build(),
            )
            val paged = mockk<ArtifactRegistryClient.ListPackagesPagedResponse>()
            every { paged.iterateAll() } returns packages

            val slot = slot<ListPackagesRequest>()
            every { client.listPackages(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListPackagesValueSource::class) {
                parameters.service.set(service)
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
