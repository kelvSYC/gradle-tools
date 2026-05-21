package com.kelvsyc.gradle.aws.java.appconfig

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionResponse
import java.io.File

class DownloadConfigurationActionSpec : FunSpec() {
    init {
        test("execute - writes configuration bytes to output file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigDataClient>()
            MockAppConfigDataClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig-data", MockAppConfigDataClientBuildService::class)

            val sessionResponse = mockk<StartConfigurationSessionResponse>()
            every { sessionResponse.initialConfigurationToken() } returns "token"
            every { client.startConfigurationSession(any<StartConfigurationSessionRequest>()) } returns sessionResponse

            val configContent = """{"feature":"enabled"}"""
            val configResponse = mockk<GetLatestConfigurationResponse>()
            every { configResponse.configuration() } returns SdkBytes.fromUtf8String(configContent)
            every { configResponse.nextPollConfigurationToken() } returns "next-token"
            every { client.getLatestConfiguration(any<GetLatestConfigurationRequest>()) } returns configResponse

            val outputFile = File.createTempFile("appconfig", ".json").also { it.deleteOnExit() }

            val params = project.objects.newInstance<DownloadConfigurationAction.Parameters>()
            params.service.set(service)
            params.applicationIdentifier.set("my-app")
            params.environmentIdentifier.set("production")
            params.configurationProfileIdentifier.set("my-profile")
            params.outputFile.set(outputFile)

            val action = object : DownloadConfigurationAction() {
                override fun getParameters() = params
            }
            action.execute()

            outputFile.readText() shouldBe configContent
        }
    }
}
