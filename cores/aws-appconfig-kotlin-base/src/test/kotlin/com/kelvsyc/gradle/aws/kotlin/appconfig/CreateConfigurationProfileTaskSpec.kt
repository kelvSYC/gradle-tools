package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.CreateConfigurationProfileRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class CreateConfigurationProfileTaskSpec : FunSpec({
    test("execute sends correct application id, name, location uri, and type") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
        coEvery { client.createConfigurationProfile(any()) } returns mockk()

        val task = project.tasks.create("t", CreateConfigurationProfileTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")
        task.profileName.set("my-config")
        task.locationUri.set("hosted")
        task.type.set("AWS.Freeform")

        task.execute()

        coVerify { client.createConfigurationProfile(any()) }
        MockAppConfigClientBuildService.mockClient = null
    }

    test("execute sends optional description when set") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig2", MockAppConfigClientBuildService::class)
        val requestSlot = slot<CreateConfigurationProfileRequest>()
        coEvery { client.createConfigurationProfile(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t2", CreateConfigurationProfileTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-456")
        task.profileName.set("prod-config")
        task.locationUri.set("hosted")
        task.type.set("AWS.AppConfig.FeatureFlags")
        task.profileDescription.set("production configuration")

        task.execute()

        requestSlot.captured.applicationId shouldBe "app-456"
        requestSlot.captured.name shouldBe "prod-config"
        requestSlot.captured.description shouldBe "production configuration"
        MockAppConfigClientBuildService.mockClient = null
    }
})
