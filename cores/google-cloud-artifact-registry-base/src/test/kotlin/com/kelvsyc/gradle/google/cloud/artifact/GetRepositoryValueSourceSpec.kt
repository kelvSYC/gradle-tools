package com.kelvsyc.gradle.google.cloud.artifact

import com.google.devtools.artifactregistry.v1.ArtifactRegistryClient
import com.google.devtools.artifactregistry.v1.GetRepositoryRequest
import com.google.devtools.artifactregistry.v1.Repository
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetRepositoryValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns Repository for the requested resource name") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ArtifactRegistryClient>()
            MockArtifactRegistryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent("ar", MockArtifactRegistryClientBuildService::class)

            val expected = Repository.newBuilder().apply {
                name = "projects/my-project/locations/us-east1/repositories/my-repo"
            }.build()
            val slot = slot<GetRepositoryRequest>()
            every { client.getRepository(capture(slot)) } returns expected

            val provider = project.providers.ofKt(GetRepositoryValueSource::class) {
                parameters.service.set(service)
                parameters.projectName.set("my-project")
                parameters.location.set("us-east1")
                parameters.repository.set("my-repo")
            }

            provider.get() shouldBe expected
            slot.captured.name shouldBe "projects/my-project/locations/us-east1/repositories/my-repo"
        }
    }
}
