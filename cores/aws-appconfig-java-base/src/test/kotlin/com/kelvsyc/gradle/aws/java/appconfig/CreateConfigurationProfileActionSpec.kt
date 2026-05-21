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
import software.amazon.awssdk.services.appconfig.model.CreateConfigurationProfileRequest
import software.amazon.awssdk.services.appconfig.model.CreateConfigurationProfileResponse

class CreateConfigurationProfileActionSpec : FunSpec() {
    init {
        test("execute - sends correct application ID, name, locationUri, and type") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigClient>()
            MockAppConfigClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig", MockAppConfigClientBuildService::class)
            val requestSlot = slot<CreateConfigurationProfileRequest>()
            every { client.createConfigurationProfile(capture(requestSlot)) } returns mockk<CreateConfigurationProfileResponse>()

            val params = project.objects.newInstance<CreateConfigurationProfileAction.Parameters>()
            params.service.set(service)
            params.applicationId.set("abc123")
            params.name.set("my-profile")
            params.locationUri.set("hosted")
            params.type.set("AWS.Freeform")

            val action = object : CreateConfigurationProfileAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.applicationId() shouldBe "abc123"
            requestSlot.captured.name() shouldBe "my-profile"
            requestSlot.captured.locationUri() shouldBe "hosted"
            requestSlot.captured.type() shouldBe "AWS.Freeform"
        }
    }
}
