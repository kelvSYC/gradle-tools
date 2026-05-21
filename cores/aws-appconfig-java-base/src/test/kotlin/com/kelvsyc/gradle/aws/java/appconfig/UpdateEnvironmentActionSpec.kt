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
import software.amazon.awssdk.services.appconfig.model.UpdateEnvironmentRequest
import software.amazon.awssdk.services.appconfig.model.UpdateEnvironmentResponse

class UpdateEnvironmentActionSpec : FunSpec() {
    init {
        test("execute - sends correct application ID, environment ID, and updated name") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<UpdateEnvironmentRequest>()
            every { client.updateEnvironment(capture(requestSlot)) } returns mockk<UpdateEnvironmentResponse>()

            val params = project.objects.newInstance<UpdateEnvironmentAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.environmentId.set("env456")
            params.name.set("staging")

            val action = object : UpdateEnvironmentAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.applicationId() shouldBe "abc123"
            requestSlot.captured.environmentId() shouldBe "env456"
            requestSlot.captured.name() shouldBe "staging"
        }
    }
}
