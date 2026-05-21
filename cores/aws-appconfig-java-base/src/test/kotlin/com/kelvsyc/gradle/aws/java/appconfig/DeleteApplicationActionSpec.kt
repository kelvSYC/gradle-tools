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
import software.amazon.awssdk.services.appconfig.model.DeleteApplicationRequest
import software.amazon.awssdk.services.appconfig.model.DeleteApplicationResponse

class DeleteApplicationActionSpec : FunSpec() {
    init {
        test("execute - sends correct application ID") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<DeleteApplicationRequest>()
            every { client.deleteApplication(capture(requestSlot)) } returns mockk<DeleteApplicationResponse>()

            val params = project.objects.newInstance<DeleteApplicationAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")

            val action = object : DeleteApplicationAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.applicationId() shouldBe "abc123"
        }
    }
}
