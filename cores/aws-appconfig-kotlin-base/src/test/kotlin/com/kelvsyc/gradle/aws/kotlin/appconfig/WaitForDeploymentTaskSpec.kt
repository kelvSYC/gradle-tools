package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.AppConfigException
import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import org.gradle.api.GradleException
import org.gradle.api.tasks.UntrackedTask
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

internal var waitForDeploymentAwaitCalled = false
internal var waitForDeploymentThrowException = false

@UntrackedTask(because = "Communicates with AWS AppConfig; no local output")
internal abstract class TestWaitForDeploymentTask : WaitForDeploymentTask() {
    override suspend fun awaitDeploymentComplete(
        client: AppConfigClient,
        applicationId: String,
        environmentId: String,
        deploymentNumber: Int,
    ) {
        waitForDeploymentAwaitCalled = true
        if (waitForDeploymentThrowException) {
            throw AppConfigException("Deployment was rolled back")
        }
    }
}

class WaitForDeploymentTaskSpec : FunSpec({
    test("waiter succeeds completes normally") {
        val project = ProjectBuilder.builder().build()
        MockAppConfigClientBuildService.mockClient = mockk()
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)

        waitForDeploymentAwaitCalled = false
        waitForDeploymentThrowException = false
        val mockTask = project.tasks.create("t1", TestWaitForDeploymentTask::class.java)
        mockTask.service.set(service)
        mockTask.applicationId.set("app-123")
        mockTask.environmentId.set("env-456")
        mockTask.deploymentNumber.set(789)

        mockTask.execute()

        waitForDeploymentAwaitCalled shouldBe true
        MockAppConfigClientBuildService.mockClient = null
    }

    test("waiter throws AppConfigException is wrapped in GradleException") {
        val project = ProjectBuilder.builder().build()
        MockAppConfigClientBuildService.mockClient = mockk()
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)

        waitForDeploymentAwaitCalled = false
        waitForDeploymentThrowException = true
        val mockTask = project.tasks.create("t2", TestWaitForDeploymentTask::class.java)
        mockTask.service.set(service)
        mockTask.applicationId.set("app-123")
        mockTask.environmentId.set("env-456")
        mockTask.deploymentNumber.set(999)

        val exception = shouldThrow<GradleException> { mockTask.execute() }
        exception.message shouldBe "Deployment 999 failed or was rolled back"
        waitForDeploymentAwaitCalled shouldBe true
        MockAppConfigClientBuildService.mockClient = null
    }
})
