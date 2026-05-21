package com.kelvsyc.gradle.aws.java.appconfig

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.services.appconfig.AppConfigClient
import software.amazon.awssdk.services.appconfig.model.CreateHostedConfigurationVersionRequest
import software.amazon.awssdk.services.appconfig.model.CreateHostedConfigurationVersionResponse
import java.io.File

class CreateHostedConfigurationVersionActionSpec : FunSpec() {
    init {
        test("execute - sends correct request and writes version number to file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<CreateHostedConfigurationVersionRequest>()
            val response = mockk<CreateHostedConfigurationVersionResponse>()
            every { response.versionNumber() } returns 3
            every { client.createHostedConfigurationVersion(capture(requestSlot)) } returns response

            val outputFile = File.createTempFile("version", ".txt").also { it.deleteOnExit() }

            val params = project.objects.newInstance<CreateHostedConfigurationVersionAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.configurationProfileId.set("prof789")
            params.content.set("""{"enabled":true}""")
            params.contentType.set("application/json")
            params.versionNumberFile.set(outputFile)

            val action = object : CreateHostedConfigurationVersionAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.applicationId() shouldBe "abc123"
            requestSlot.captured.configurationProfileId() shouldBe "prof789"
            requestSlot.captured.contentType() shouldBe "application/json"
            outputFile.readText() shouldBe "3"
        }
    }
}
