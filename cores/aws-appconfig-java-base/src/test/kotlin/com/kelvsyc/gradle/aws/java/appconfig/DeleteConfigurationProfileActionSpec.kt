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
import software.amazon.awssdk.services.appconfig.model.DeleteConfigurationProfileRequest
import software.amazon.awssdk.services.appconfig.model.DeleteConfigurationProfileResponse

class DeleteConfigurationProfileActionSpec : FunSpec() {
    init {
        test("execute - sends correct application ID and configuration profile ID") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<DeleteConfigurationProfileRequest>()
            every { client.deleteConfigurationProfile(capture(requestSlot)) } returns mockk<DeleteConfigurationProfileResponse>()

            val params = project.objects.newInstance<DeleteConfigurationProfileAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.configurationProfileId.set("prof789")

            val action = object : DeleteConfigurationProfileAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.applicationId() shouldBe "abc123"
            requestSlot.captured.configurationProfileId() shouldBe "prof789"
        }
    }
}
