package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.UpdateConfigurationProfileRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UpdateConfigurationProfileTaskSpec : FunSpec({
    test("execute sends correct application id and configuration profile id") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
        coEvery { client.updateConfigurationProfile(any()) } returns mockk()

        val task = project.tasks.create("t", UpdateConfigurationProfileTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")
        task.configurationProfileId.set("cp-456")

        task.execute()

        coVerify { client.updateConfigurationProfile(any()) }
        MockAppConfigClientBuildService.mockClient = null
    }

    test("execute sends optional name and description when set") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig2", MockAppConfigClientBuildService::class)
        val requestSlot = slot<UpdateConfigurationProfileRequest>()
        coEvery { client.updateConfigurationProfile(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t2", UpdateConfigurationProfileTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-789")
        task.configurationProfileId.set("cp-012")
        task.profileName.set("updated-config")
        task.profileDescription.set("updated description")

        task.execute()

        requestSlot.captured.applicationId shouldBe "app-789"
        requestSlot.captured.configurationProfileId shouldBe "cp-012"
        requestSlot.captured.name shouldBe "updated-config"
        requestSlot.captured.description shouldBe "updated description"
        MockAppConfigClientBuildService.mockClient = null
    }
})
