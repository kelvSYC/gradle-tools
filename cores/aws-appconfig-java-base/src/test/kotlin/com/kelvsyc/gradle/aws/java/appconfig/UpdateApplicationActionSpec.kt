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
import software.amazon.awssdk.services.appconfig.model.UpdateApplicationRequest
import software.amazon.awssdk.services.appconfig.model.UpdateApplicationResponse

class UpdateApplicationActionSpec : FunSpec() {
    init {
        test("execute - sends correct application ID and updated name") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<UpdateApplicationRequest>()
            every { client.updateApplication(capture(requestSlot)) } returns mockk<UpdateApplicationResponse>()

            val params = project.objects.newInstance<UpdateApplicationAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.name.set("renamed-application")

            val action = object : UpdateApplicationAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.applicationId() shouldBe "abc123"
            requestSlot.captured.name() shouldBe "renamed-application"
        }
    }
}
