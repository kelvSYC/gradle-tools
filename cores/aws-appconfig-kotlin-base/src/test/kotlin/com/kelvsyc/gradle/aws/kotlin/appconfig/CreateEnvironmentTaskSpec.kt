package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.CreateEnvironmentRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class CreateEnvironmentTaskSpec : FunSpec({
    test("execute sends correct application id and environment name") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
        coEvery { client.createEnvironment(any()) } returns mockk()

        val task = project.tasks.create("t", CreateEnvironmentTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")
        task.environmentName.set("production")

        task.execute()

        coVerify { client.createEnvironment(any()) }
        MockAppConfigClientBuildService.mockClient = null
    }

    test("execute sends optional description when set") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig2", MockAppConfigClientBuildService::class)
        val requestSlot = slot<CreateEnvironmentRequest>()
        coEvery { client.createEnvironment(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t2", CreateEnvironmentTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-456")
        task.environmentName.set("staging")
        task.environmentDescription.set("staging environment")

        task.execute()

        requestSlot.captured.applicationId shouldBe "app-456"
        requestSlot.captured.name shouldBe "staging"
        requestSlot.captured.description shouldBe "staging environment"
        MockAppConfigClientBuildService.mockClient = null
    }
})
