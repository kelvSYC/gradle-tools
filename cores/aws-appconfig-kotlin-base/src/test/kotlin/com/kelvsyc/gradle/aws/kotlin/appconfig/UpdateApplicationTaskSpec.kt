package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.UpdateApplicationRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UpdateApplicationTaskSpec : FunSpec({
    test("execute sends correct application id") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
        coEvery { client.updateApplication(any()) } returns mockk()

        val task = project.tasks.create("t", UpdateApplicationTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")

        task.execute()

        coVerify { client.updateApplication(any()) }
        MockAppConfigClientBuildService.mockClient = null
    }

    test("execute sends optional name and description when set") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig2", MockAppConfigClientBuildService::class)
        val requestSlot = slot<UpdateApplicationRequest>()
        coEvery { client.updateApplication(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t2", UpdateApplicationTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-456")
        task.applicationName.set("new-name")
        task.applicationDescription.set("new description")

        task.execute()

        requestSlot.captured.applicationId shouldBe "app-456"
        requestSlot.captured.name shouldBe "new-name"
        requestSlot.captured.description shouldBe "new description"
        MockAppConfigClientBuildService.mockClient = null
    }
})
