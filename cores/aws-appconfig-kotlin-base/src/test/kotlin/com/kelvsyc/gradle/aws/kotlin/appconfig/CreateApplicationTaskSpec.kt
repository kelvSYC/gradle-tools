package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.CreateApplicationRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class CreateApplicationTaskSpec : FunSpec({
    test("execute sends correct application name") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
        coEvery { client.createApplication(any()) } returns mockk()

        val task = project.tasks.create("t", CreateApplicationTask::class.java)
        task.service.set(service)
        task.applicationName.set("my-application")

        task.execute()

        coVerify { client.createApplication(any()) }
        MockAppConfigClientBuildService.mockClient = null
    }

    test("execute sends optional description when set") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig2", MockAppConfigClientBuildService::class)
        val requestSlot = slot<CreateApplicationRequest>()
        coEvery { client.createApplication(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t2", CreateApplicationTask::class.java)
        task.service.set(service)
        task.applicationName.set("my-app")
        task.applicationDescription.set("my description")

        task.execute()

        requestSlot.captured.name shouldBe "my-app"
        requestSlot.captured.description shouldBe "my description"
        MockAppConfigClientBuildService.mockClient = null
    }
})
