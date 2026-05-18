package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRepository
import com.azure.containers.containerregistry.RegistryArtifact
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeleteManifestActionSpec : FunSpec() {
    init {
        test("execute - navigates repository to artifact and deletes manifest") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ContainerRepository>()
            MockContainerRepositoryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "repository-service",
                MockContainerRepositoryClientBuildService::class
            )
            val mockArtifact = mockk<RegistryArtifact>()
            every { client.getArtifact("sha256:abc123def456") } returns mockArtifact
            justRun { mockArtifact.delete() }

            val params = project.objects.newInstance<DeleteManifestAction.Parameters>()
            params.service.set(service)
            params.digest.set("sha256:abc123def456")

            val action = object : DeleteManifestAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { client.getArtifact("sha256:abc123def456") }
            verify { mockArtifact.delete() }
        }
    }
}
