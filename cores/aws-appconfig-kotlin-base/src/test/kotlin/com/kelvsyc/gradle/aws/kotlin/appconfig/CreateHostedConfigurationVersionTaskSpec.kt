package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfig.AppConfigClient
import aws.sdk.kotlin.services.appconfig.model.CreateHostedConfigurationVersionResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class CreateHostedConfigurationVersionTaskSpec : FunSpec({
    test("execute calls SDK and writes version number to file") {
        val project = ProjectBuilder.builder().build()
        val client = mockk<AppConfigClient>()
        MockAppConfigClientBuildService.mockClient = client
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
        coEvery { client.createHostedConfigurationVersion(any()) } returns
            CreateHostedConfigurationVersionResponse { versionNumber = 42 }

        val outFile = project.layout.buildDirectory.file("version.txt").get().asFile
        outFile.parentFile.mkdirs()

        val task = project.tasks.create("t", CreateHostedConfigurationVersionTask::class.java)
        task.service.set(service)
        task.applicationId.set("app-123")
        task.configurationProfileId.set("profile-456")
        task.content.set("{\"key\": \"value\"}")
        task.contentType.set("application/json")
        task.versionNumberFile.set(outFile)

        task.execute()

        coVerify { client.createHostedConfigurationVersion(any()) }
        task.versionNumberFile.get().asFile.readText() shouldBe "42"
        MockAppConfigClientBuildService.mockClient = null
    }
})
