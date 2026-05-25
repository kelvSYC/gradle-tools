package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.models.Container
import com.azure.resourcemanager.appcontainers.models.Job
import com.azure.resourcemanager.appcontainers.models.JobTemplate
import com.kelvsyc.gradle.azure.containerapp.actions.UpdateContainerAppJobAction
import io.kotest.core.spec.style.FunSpec
import io.mockk.every
import io.mockk.mockk
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UpdateContainerAppJobActionSpec : FunSpec() {
    init {
        afterTest {
            MockContainerAppJobBuildService.mockJob = null
        }

        test("execute - updates job container image") {
            val project = ProjectBuilder.builder().build()
            val job = mockk<Job>()
            MockContainerAppJobBuildService.mockJob = job
            val service = project.gradle.sharedServices.registerIfAbsent(
                "containerAppJob",
                MockContainerAppJobBuildService::class,
            )

            val container = mockk<Container>(relaxed = true)
            val template = mockk<JobTemplate>(relaxed = true)
            val updateStage = mockk<Job.Update>(relaxed = true)
            every { job.template() } returns template
            every { template.containers() } returns listOf(container)
            every { container.withImage(any()) } returns container
            every { container.withEnv(any()) } returns container
            every { template.withContainers(any()) } returns template
            every { job.update() } returns updateStage
            every { updateStage.apply() } returns job

            val params = project.objects.newInstance<UpdateContainerAppJobAction.Parameters>()
            params.service.set(service)
            params.imageUri.set("myregistry.azurecr.io/myjob:2.0")

            val action = object : UpdateContainerAppJobAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { container.withImage("myregistry.azurecr.io/myjob:2.0") }
            verify { updateStage.apply() }
        }
    }
}
