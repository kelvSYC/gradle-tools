package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.ListFilesRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import com.google.devtools.artifactregistry.v1.File as ArtifactRegistryFile

class ListFilesValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns file resource names and propagates filter when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ArtifactRegistryClient>()
            MockArtifactRegistryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ar", MockArtifactRegistryClientBuildService::class)

            val files = listOf(
                ArtifactRegistryFile.newBuilder().setName("projects/p/locations/us-east1/repositories/r/files/foo.txt").build(),
            )
            val paged = mockk<ArtifactRegistryClient.ListFilesPagedResponse>()
            every { paged.iterateAll() } returns files

            val slot = slot<ListFilesRequest>()
            every { client.listFiles(capture(slot)) } returns paged

            val provider = project.providers.ofKt(ListFilesValueSource::class) {
                parameters.service.set(service)
                parameters.projectName.set("p")
                parameters.location.set("us-east1")
                parameters.repository.set("r")
                parameters.filter.set("owner=\"projects/p/locations/us-east1/repositories/r/packages/pkg/versions/1.0.0\"")
            }

            provider.get() shouldBe listOf("projects/p/locations/us-east1/repositories/r/files/foo.txt")
            slot.captured.parent shouldBe "projects/p/locations/us-east1/repositories/r"
            slot.captured.filter shouldBe "owner=\"projects/p/locations/us-east1/repositories/r/packages/pkg/versions/1.0.0\""
        }
    }
}
