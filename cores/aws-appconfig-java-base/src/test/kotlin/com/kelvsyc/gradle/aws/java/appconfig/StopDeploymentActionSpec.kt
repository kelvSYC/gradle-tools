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
import software.amazon.awssdk.services.appconfig.model.StopDeploymentRequest
import software.amazon.awssdk.services.appconfig.model.StopDeploymentResponse

class StopDeploymentActionSpec : FunSpec() {
    init {
        test("execute - sends correct application ID, environment ID, and deployment number") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<StopDeploymentRequest>()
            every { client.stopDeployment(capture(requestSlot)) } returns mockk<StopDeploymentResponse>()

            val params = project.objects.newInstance<StopDeploymentAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.environmentId.set("env456")
            params.deploymentNumber.set(7)

            val action = object : StopDeploymentAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.applicationId() shouldBe "abc123"
            requestSlot.captured.environmentId() shouldBe "env456"
            requestSlot.captured.deploymentNumber() shouldBe 7
        }
    }
}
