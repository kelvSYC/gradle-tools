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
import software.amazon.awssdk.services.appconfig.model.StartDeploymentRequest
import software.amazon.awssdk.services.appconfig.model.StartDeploymentResponse
import java.io.File

class StartDeploymentActionSpec : FunSpec() {
    init {
        test("execute - sends correct request and writes deployment number to file") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<StartDeploymentRequest>()
            val response = mockk<StartDeploymentResponse>()
            every { response.deploymentNumber() } returns 7
            every { client.startDeployment(capture(requestSlot)) } returns response

            val outputFile = File.createTempFile("deployment", ".txt").also { it.deleteOnExit() }

            val params = project.objects.newInstance<StartDeploymentAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.environmentId.set("env456")
            params.configurationProfileId.set("prof789")
            params.deploymentStrategyId.set("strategy-id")
            params.configurationVersion.set("3")
            params.deploymentNumberFile.set(outputFile)

            val action = object : StartDeploymentAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.applicationId() shouldBe "abc123"
            requestSlot.captured.environmentId() shouldBe "env456"
            requestSlot.captured.configurationProfileId() shouldBe "prof789"
            requestSlot.captured.deploymentStrategyId() shouldBe "strategy-id"
            requestSlot.captured.configurationVersion() shouldBe "3"
            outputFile.readText() shouldBe "7"
        }
    }
}
