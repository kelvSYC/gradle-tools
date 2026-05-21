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
import software.amazon.awssdk.services.appconfig.model.CreateApplicationRequest
import software.amazon.awssdk.services.appconfig.model.CreateApplicationResponse

class CreateApplicationActionSpec : FunSpec() {
    init {
        test("execute - sends correct application name and description") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<CreateApplicationRequest>()
            every { client.createApplication(capture(requestSlot)) } returns mockk<CreateApplicationResponse>()

            val params = project.objects.newInstance<CreateApplicationAction.Parameters>()
            params.service.set(service)
            params.name.set("my-application")
            params.description.set("My application description")

            val action = object : CreateApplicationAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.name() shouldBe "my-application"
            requestSlot.captured.description() shouldBe "My application description"
        }
    }
}
