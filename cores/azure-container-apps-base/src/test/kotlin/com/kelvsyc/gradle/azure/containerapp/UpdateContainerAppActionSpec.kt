package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.models.Container
import com.azure.resourcemanager.appcontainers.models.ContainerApp
import com.azure.resourcemanager.appcontainers.models.Template
import com.kelvsyc.gradle.azure.containerapp.actions.UpdateContainerAppAction
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UpdateContainerAppActionSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppBuildService.mockApp = null
        }

        test("execute - updates container image") {
            val project = ProjectBuilder.builder().build()
            val app = mockk<ContainerApp>()
            MockContainerAppBuildService.mockApp = app
            val service = project.gradle.sharedServices.registerIfAbsent(
                "containerApp",
                MockContainerAppBuildService::class,
            )

            val container = mockk<Container>(relaxed = true)
            val template = mockk<Template>(relaxed = true)
            val updateStage = mockk<ContainerApp.Update>(relaxed = true)
            every { app.template() } returns template
            every { template.containers() } returns listOf(container)
            every { container.withImage(any()) } returns container
            every { container.withEnv(any()) } returns container
            every { template.withContainers(any()) } returns template
            every { app.update() } returns updateStage
            every { updateStage.withTemplate(any()) } returns updateStage
            every { updateStage.apply() } returns app

            val params = project.objects.newInstance<UpdateContainerAppAction.Parameters>()
            params.service.set(service)
            params.imageUri.set("myregistry.azurecr.io/myapp:2.0")

            val action = object : UpdateContainerAppAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { container.withImage("myregistry.azurecr.io/myapp:2.0") }
            verify { updateStage.apply() }
        }
    }
}
