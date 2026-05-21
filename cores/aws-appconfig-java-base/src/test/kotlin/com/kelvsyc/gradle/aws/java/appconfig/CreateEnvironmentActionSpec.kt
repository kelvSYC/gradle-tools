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
import software.amazon.awssdk.services.appconfig.model.CreateEnvironmentRequest
import software.amazon.awssdk.services.appconfig.model.CreateEnvironmentResponse

class CreateEnvironmentActionSpec : FunSpec() {
    init {
        test("execute - sends correct application ID, environment name, and description") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<CreateEnvironmentRequest>()
            every { client.createEnvironment(capture(requestSlot)) } returns mockk<CreateEnvironmentResponse>()

            val params = project.objects.newInstance<CreateEnvironmentAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.name.set("production")
            params.description.set("Production environment")

            val action = object : CreateEnvironmentAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.applicationId() shouldBe "abc123"
            requestSlot.captured.name() shouldBe "production"
            requestSlot.captured.description() shouldBe "Production environment"
        }
    }
}
