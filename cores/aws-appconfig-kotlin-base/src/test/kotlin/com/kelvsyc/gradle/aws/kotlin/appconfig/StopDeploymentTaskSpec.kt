package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.StopDeploymentRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class StopDeploymentTaskSpec : FunSpec({
    test("execute calls stopDeployment with correct parameters") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
        val requestSlot = slot<StopDeploymentRequest>()
        coEvery { client.stopDeployment(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t", StopDeploymentTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")
        task.environmentId.set("env-456")
        task.deploymentNumber.set(789)

        task.execute()

        coVerify { client.stopDeployment(any()) }
        requestSlot.captured.applicationId shouldBe "app-123"
        requestSlot.captured.environmentId shouldBe "env-456"
        requestSlot.captured.deploymentNumber shouldBe 789
        MockAppConfigClientBuildService.mockClient = null
    }
})
