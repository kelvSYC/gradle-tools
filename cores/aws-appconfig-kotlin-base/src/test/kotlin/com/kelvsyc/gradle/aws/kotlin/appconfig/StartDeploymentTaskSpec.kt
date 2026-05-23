package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.AppConfigException
import aws.sdk.kotlin.services.appconfig.model.StartDeploymentResponse
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.gradle.api.GradleException
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import org.gradle.work.DisableCachingByDefault

internal var startDeploymentAwaitCalled = false
internal var startDeploymentAwaitExceptionThrown = false

@DisableCachingByDefault(because = "Communicates with AWS AppConfig; result depends on external state")
internal abstract class TestStartDeploymentTaskWithWaitOverride : StartDeploymentTask() {
    override suspend fun awaitDeploymentComplete(
        client: AppConfigClient,
        applicationId: String,
        environmentId: String,
        deploymentNumber: Int,
    ) {
        startDeploymentAwaitCalled = true
        if (startDeploymentAwaitExceptionThrown) {
            throw AppConfigException("Deployment failed")
        }
    }
}

class StartDeploymentTaskSpec : FunSpec({
    test("with deploymentNumberFile present, writes deployment number and does not await") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
        coEvery { client.startDeployment(any()) } returns
            StartDeploymentResponse { deploymentNumber = 123 }

        val outFile = project.layout.buildDirectory.file("deployment.txt").get().asFile
        outFile.parentFile.mkdirs()

        val task = project.tasks.create("t1", StartDeploymentTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")
        task.environmentId.set("env-456")
        task.configurationProfileId.set("profile-789")
        task.deploymentStrategyId.set("strategy-111")
        task.configurationVersion.set("1")
        task.deploymentNumberFile.set(outFile)

        task.execute()

        coVerify { client.startDeployment(any()) }
        task.deploymentNumberFile.get().asFile.readText() shouldBe "123"
        MockAppConfigClientBuildService.mockClient = null
    }

    test("without deploymentNumberFile, completes without error") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig2", MockAppConfigClientBuildService::class)
        coEvery { client.startDeployment(any()) } returns
            StartDeploymentResponse { deploymentNumber = 456 }

        startDeploymentAwaitCalled = false
        startDeploymentAwaitExceptionThrown = false
        val task = project.tasks.create("t2", TestStartDeploymentTaskWithWaitOverride::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")
        task.environmentId.set("env-456")
        task.configurationProfileId.set("profile-789")
        task.deploymentStrategyId.set("strategy-111")
        task.configurationVersion.set("1")

        task.execute()

        coVerify { client.startDeployment(any()) }
        startDeploymentAwaitCalled shouldBe true
        MockAppConfigClientBuildService.mockClient = null
    }

    test("without deploymentNumberFile, wraps waiter exceptions in GradleException") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig3", MockAppConfigClientBuildService::class)
        coEvery { client.startDeployment(any()) } returns
            StartDeploymentResponse { deploymentNumber = 789 }

        startDeploymentAwaitCalled = false
        startDeploymentAwaitExceptionThrown = true
        val task = project.tasks.create("t3", TestStartDeploymentTaskWithWaitOverride::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")
        task.environmentId.set("env-456")
        task.configurationProfileId.set("profile-789")
        task.deploymentStrategyId.set("strategy-111")
        task.configurationVersion.set("1")

        val exception = shouldThrow<GradleException> { task.execute() }
        exception.message shouldBe "Deployment 789 failed or was rolled back"
        startDeploymentAwaitCalled shouldBe true
        MockAppConfigClientBuildService.mockClient = null
    }
})
