package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRepository
import com.azure.containers.containerregistry.models.ContainerRepositoryProperties
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UpdateRepositoryPropertiesActionSpec : FunSpec() {
    init {
        test("execute - updates repository properties with correct flags") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ContainerRepository>()
            MockContainerRepositoryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "repository-service",
                MockContainerRepositoryClientBuildService::class
            )
            val propsSlot = slot<ContainerRepositoryProperties>()
            every { client.updateProperties(capture(propsSlot)) } returns mockk()

            val params = project.objects.newInstance<UpdateRepositoryPropertiesAction.Parameters>()
            params.service.set(service)
            params.canWrite.set(true)
            params.canDelete.set(false)
            params.canList.set(true)

            val action = object : UpdateRepositoryPropertiesAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { client.updateProperties(any()) }
            val capturedProps = propsSlot.captured
            assert(capturedProps.isWriteEnabled == true)
            assert(capturedProps.isDeleteEnabled == false)
            assert(capturedProps.isListEnabled == true)
        }
    }
}
