package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRepository
import com.azure.containers.containerregistry.RegistryArtifact
import com.azure.containers.containerregistry.models.ArtifactTagProperties
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UpdateTagPropertiesActionSpec : FunSpec() {
    init {
        test("execute - updates tag properties with correct flags") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ContainerRepository>()
            MockContainerRepositoryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "repository-service",
                MockContainerRepositoryClientBuildService::class
            )
            val mockArtifact = mockk<RegistryArtifact>()
            val propsSlot = slot<ArtifactTagProperties>()
            every { client.getArtifact("v1.0.0") } returns mockArtifact
            every { mockArtifact.updateTagProperties("v1.0.0", capture(propsSlot)) } returns mockk()

            val params = project.objects.newInstance<UpdateTagPropertiesAction.Parameters>()
            params.service.set(service)
            params.tagName.set("v1.0.0")
            params.canWrite.set(false)
            params.canDelete.set(true)

            val action = object : UpdateTagPropertiesAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { client.getArtifact("v1.0.0") }
            verify { mockArtifact.updateTagProperties("v1.0.0", any()) }
            val capturedProps = propsSlot.captured
            assert(capturedProps.isWriteEnabled == false)
            assert(capturedProps.isDeleteEnabled == true)
        }
    }
}
