package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.models.Configuration
import com.azure.resourcemanager.appcontainers.models.ContainerApp
import com.azure.resourcemanager.appcontainers.models.Ingress
import com.kelvsyc.gradle.azure.containerapp.sources.GetContainerAppValueSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetContainerAppValueSourceSpec : FunSpec() {
    init {
        afterTest { MockContainerAppBuildService.mockApp = null }

        test("obtain - returns ingress FQDN") {
            val project = ProjectBuilder.builder().build()
            val app = mockk<ContainerApp>()
            MockContainerAppBuildService.mockApp = app
            val service = project.gradle.sharedServices.registerIfAbsent("containerApp", MockContainerAppBuildService::class)

            val ingress = mockk<Ingress>()
            val config = mockk<Configuration>()
            every { app.configuration() } returns config
            every { config.ingress() } returns ingress
            every { ingress.fqdn() } returns "my-app.eastus.azurecontainerapps.io"

            val provider = project.providers.ofKt(GetContainerAppValueSource::class) {
                parameters.service.set(service)
            }
            provider.orNull shouldBe "my-app.eastus.azurecontainerapps.io"
        }
    }
}
