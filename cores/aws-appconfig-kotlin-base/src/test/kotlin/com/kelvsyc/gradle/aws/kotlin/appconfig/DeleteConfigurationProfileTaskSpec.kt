package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.DeleteConfigurationProfileRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeleteConfigurationProfileTaskSpec : FunSpec({
    test("execute sends correct application id and configuration profile id") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
        val requestSlot = slot<DeleteConfigurationProfileRequest>()
        coEvery { client.deleteConfigurationProfile(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t", DeleteConfigurationProfileTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")
        task.configurationProfileId.set("cp-456")

        task.execute()

        requestSlot.captured.applicationId shouldBe "app-123"
        requestSlot.captured.configurationProfileId shouldBe "cp-456"
        MockAppConfigClientBuildService.mockClient = null
    }
})
