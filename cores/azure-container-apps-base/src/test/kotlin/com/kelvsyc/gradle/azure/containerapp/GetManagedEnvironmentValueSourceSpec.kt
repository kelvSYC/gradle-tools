package com.kelvsyc.gradle.azure.containerapp

import com.azure.resourcemanager.appcontainers.ContainerAppsApiManager
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironment
import com.azure.resourcemanager.appcontainers.models.ManagedEnvironments
import com.kelvsyc.gradle.azure.containerapp.sources.GetManagedEnvironmentValueSource
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetManagedEnvironmentValueSourceSpec : FunSpec() {
    init {
        afterTest { MockContainerAppsEnvironmentBuildService.mockManager = null }

        test("obtain - returns default domain") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<ContainerAppsApiManager>()
            MockContainerAppsEnvironmentBuildService.mockManager = manager
            val service = project.gradle.sharedServices.registerIfAbsent(
                "containerAppsEnv", MockContainerAppsEnvironmentBuildService::class,
            ) { spec ->
                spec.parameters.resourceGroupName.set("my-rg")
                spec.parameters.environmentName.set("my-env")
                spec.parameters.subscriptionId.set("sub-id")
            }

            val env = mockk<ManagedEnvironment>()
            val managedEnvs = mockk<ManagedEnvironments>()
            every { manager.managedEnvironments() } returns managedEnvs
            every { managedEnvs.getByResourceGroup("my-rg", "my-env") } returns env
            every { env.defaultDomain() } returns "my-env.eastus.azurecontainerapps.io"

            val provider = project.providers.ofKt(GetManagedEnvironmentValueSource::class) {
                parameters.service.set(service)
            }
            provider.orNull shouldBe "my-env.eastus.azurecontainerapps.io"
        }
    }
}
