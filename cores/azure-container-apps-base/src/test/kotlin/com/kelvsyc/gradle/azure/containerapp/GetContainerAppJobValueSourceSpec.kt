package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.models.Job
import com.kelvsyc.gradle.azure.containerapp.sources.GetContainerAppJobValueSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetContainerAppJobValueSourceSpec : FunSpec() {
    init {
        afterTest { MockContainerAppJobBuildService.mockJob = null }

        test("obtain - returns provisioning state") {
            val project = ProjectBuilder.builder().build()
            val job = mockk<Job>()
            MockContainerAppJobBuildService.mockJob = job
            val service = project.gradle.sharedServices.registerIfAbsent("containerAppJob", MockContainerAppJobBuildService::class)

            every { job.provisioningState().toString() } returns "Succeeded"

            val provider = project.providers.ofKt(GetContainerAppJobValueSource::class) {
                parameters.service.set(service)
            }
            provider.orNull shouldBe "Succeeded"
        }
    }
}
