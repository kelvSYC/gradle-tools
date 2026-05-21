package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.ConfigurationClient
import com.azure.data.appconfiguration.models.ConfigurationSetting
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SetConfigurationSettingActionSpec : FunSpec() {
    init {
        test("execute - sets key and value") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val settingSlot = slot<ConfigurationSetting>()
            every { client.setConfigurationSetting(capture(settingSlot)) } returns mockk()

            val params = project.objects.newInstance<SetConfigurationSettingAction.Parameters>()
            params.service.set(service)
            params.key.set("app.name")
            params.value.set("my-app")

            val action = object : SetConfigurationSettingAction() {
                override fun getParameters() = params
            }
            action.execute()

            settingSlot.captured.getKey() shouldBe "app.name"
            settingSlot.captured.getValue() shouldBe "my-app"
        }

        test("execute - sets label when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val settingSlot = slot<ConfigurationSetting>()
            every { client.setConfigurationSetting(capture(settingSlot)) } returns mockk()

            val params = project.objects.newInstance<SetConfigurationSettingAction.Parameters>()
            params.service.set(service)
            params.key.set("app.config")
            params.value.set("prod-value")
            params.label.set("prod")

            val action = object : SetConfigurationSettingAction() {
                override fun getParameters() = params
            }
            action.execute()

            settingSlot.captured.getKey() shouldBe "app.config"
            settingSlot.captured.getValue() shouldBe "prod-value"
            settingSlot.captured.getLabel() shouldBe "prod"
        }

        test("execute - sets content type when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val settingSlot = slot<ConfigurationSetting>()
            every { client.setConfigurationSetting(capture(settingSlot)) } returns mockk()

            val params = project.objects.newInstance<SetConfigurationSettingAction.Parameters>()
            params.service.set(service)
            params.key.set("app.json")
            params.value.set("{\"timeout\": 30}")
            params.contentType.set("application/json")

            val action = object : SetConfigurationSettingAction() {
                override fun getParameters() = params
            }
            action.execute()

            settingSlot.captured.getKey() shouldBe "app.json"
            settingSlot.captured.getValue() shouldBe "{\"timeout\": 30}"
            settingSlot.captured.getContentType() shouldBe "application/json"
        }
    }
}
