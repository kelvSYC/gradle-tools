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
import software.amazon.awssdk.services.appconfig.model.DeleteEnvironmentRequest
import software.amazon.awssdk.services.appconfig.model.DeleteEnvironmentResponse

class DeleteEnvironmentActionSpec : FunSpec() {
    init {
        test("execute - sends correct application ID and environment ID") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<DeleteEnvironmentRequest>()
            every { client.deleteEnvironment(capture(requestSlot)) } returns mockk<DeleteEnvironmentResponse>()

            val params = project.objects.newInstance<DeleteEnvironmentAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.environmentId.set("env456")

            val action = object : DeleteEnvironmentAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.applicationId() shouldBe "abc123"
            requestSlot.captured.environmentId() shouldBe "env456"
        }
    }
}
