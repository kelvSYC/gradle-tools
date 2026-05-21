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
import software.amazon.awssdk.services.appconfig.model.UpdateConfigurationProfileRequest
import software.amazon.awssdk.services.appconfig.model.UpdateConfigurationProfileResponse

class UpdateConfigurationProfileActionSpec : FunSpec() {
    init {
        test("execute - sends correct application ID, profile ID, and updated name") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<UpdateConfigurationProfileRequest>()
            every { client.updateConfigurationProfile(capture(requestSlot)) } returns mockk<UpdateConfigurationProfileResponse>()

            val params = project.objects.newInstance<UpdateConfigurationProfileAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.configurationProfileId.set("prof789")
            params.name.set("renamed-profile")

            val action = object : UpdateConfigurationProfileAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.applicationId() shouldBe "abc123"
            requestSlot.captured.configurationProfileId() shouldBe "prof789"
            requestSlot.captured.name() shouldBe "renamed-profile"
        }
    }
}
