package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.DeleteApplicationRequest
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class DeleteApplicationTaskSpec : FunSpec({
    test("execute sends correct application id") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
        val requestSlot = slot<DeleteApplicationRequest>()
        coEvery { client.deleteApplication(capture(requestSlot)) } returns mockk()

        val task = project.tasks.create("t", DeleteApplicationTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")

        task.execute()

        requestSlot.captured.applicationId shouldBe "app-123"
        MockAppConfigClientBuildService.mockClient = null
    }
})
