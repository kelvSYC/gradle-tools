package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.UpdateEnvironmentRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UpdateEnvironmentTaskSpec : FunSpec({
    test("execute sends correct application id and environment id") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
        coEvery { client.updateEnvironment(any()) } returns mockk()

        val task = project.tasks.create("t", UpdateEnvironmentTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")
        task.environmentId.set("env-456")

        task.execute()

        coVerify { client.updateEnvironment(any()) }
        MockAppConfigClientBuildService.mockClient = null
    }

    test("execute sends optional name and description when set") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig2", MockAppConfigClientBuildService::class)
        val requestSlot = slot<UpdateEnvironmentRequest>()
        coEvery { client.updateEnvironment(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t2", UpdateEnvironmentTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-789")
        task.environmentId.set("env-012")
        task.environmentName.set("new-env-name")
        task.environmentDescription.set("new env description")

        task.execute()

        requestSlot.captured.applicationId shouldBe "app-789"
        requestSlot.captured.environmentId shouldBe "env-012"
        requestSlot.captured.name shouldBe "new-env-name"
        requestSlot.captured.description shouldBe "new env description"
        MockAppConfigClientBuildService.mockClient = null
    }
})
