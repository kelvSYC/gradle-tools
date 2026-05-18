package com.kelvsyc.gradle.azure.containerregistry

import com.azure.containers.containerregistry.ContainerRegistryClient
import io.kotest.core.spec.style.FunSpec
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeleteRepositoryActionSpec : FunSpec() {
    init {
        test("execute - deletes repository using registry client") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ContainerRegistryClient>()
            MockContainerRegistryClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "registry-service",
                MockContainerRegistryClientBuildService::class
            )
            justRun { client.deleteRepository("my-repo") }

            val params = project.objects.newInstance<DeleteRepositoryAction.Parameters>()
            params.service.set(service)
            params.repositoryName.set("my-repo")

            val action = object : DeleteRepositoryAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { client.deleteRepository("my-repo") }
        }
    }
}
