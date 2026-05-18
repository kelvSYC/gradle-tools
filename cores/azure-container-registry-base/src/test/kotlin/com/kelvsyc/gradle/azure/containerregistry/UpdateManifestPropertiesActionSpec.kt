package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRepository
import com.azure.containers.containerregistry.RegistryArtifact
import com.azure.containers.containerregistry.models.ArtifactManifestProperties
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UpdateManifestPropertiesActionSpec : FunSpec() {
    init {
        test("execute - updates manifest properties with correct flags") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ContainerRepository>()
            MockContainerRepositoryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "repository-service",
                MockContainerRepositoryClientBuildService::class
            )
            val mockArtifact = mockk<RegistryArtifact>()
            val propsSlot = slot<ArtifactManifestProperties>()
            every { client.getArtifact("sha256:abc123def456") } returns mockArtifact
            every { mockArtifact.updateManifestProperties(capture(propsSlot)) } returns mockk()

            val params = project.objects.newInstance<UpdateManifestPropertiesAction.Parameters>()
            params.service.set(service)
            params.digest.set("sha256:abc123def456")
            params.canWrite.set(true)
            params.canDelete.set(false)
            params.canList.set(true)
            params.canRead.set(false)

            val action = object : UpdateManifestPropertiesAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { client.getArtifact("sha256:abc123def456") }
            verify { mockArtifact.updateManifestProperties(any()) }
            val capturedProps = propsSlot.captured
            assert(capturedProps.isWriteEnabled == true)
            assert(capturedProps.isDeleteEnabled == false)
            assert(capturedProps.isListEnabled == true)
            assert(capturedProps.isReadEnabled == false)
        }
    }
}
