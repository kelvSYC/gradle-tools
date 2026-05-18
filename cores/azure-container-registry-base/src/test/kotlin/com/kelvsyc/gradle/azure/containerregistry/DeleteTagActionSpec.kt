package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRepository
import com.azure.containers.containerregistry.RegistryArtifact
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeleteTagActionSpec : FunSpec() {
    init {
        test("execute - navigates repository to artifact and deletes tag") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ContainerRepository>()
            MockContainerRepositoryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "repository-service",
                MockContainerRepositoryClientBuildService::class
            )
            val mockArtifact = mockk<RegistryArtifact>()
            every { client.getArtifact("v1.0.0") } returns mockArtifact
            justRun { mockArtifact.deleteTag("v1.0.0") }

            val params = project.objects.newInstance<DeleteTagAction.Parameters>()
            params.service.set(service)
            params.tagName.set("v1.0.0")

            val action = object : DeleteTagAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { client.getArtifact("v1.0.0") }
            verify { mockArtifact.deleteTag("v1.0.0") }
        }
    }
}
