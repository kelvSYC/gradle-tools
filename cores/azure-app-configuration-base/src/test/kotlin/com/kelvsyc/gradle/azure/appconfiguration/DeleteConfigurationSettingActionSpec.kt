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

class DeleteConfigurationSettingActionSpec : FunSpec() {
    init {
        test("execute - deletes by key and null label") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val keySlot = slot<String>()
            every { client.deleteConfigurationSetting(capture(keySlot), isNull()) } returns mockk<ConfigurationSetting>()

            val params = project.objects.newInstance<DeleteConfigurationSettingAction.Parameters>()
            params.service.set(service)
            params.key.set("app.old")

            val action = object : DeleteConfigurationSettingAction() {
                override fun getParameters() = params
            }
            action.execute()

            keySlot.captured shouldBe "app.old"
        }

        test("execute - passes label when set") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val keySlot = slot<String>()
            val labelSlot = slot<String>()
            every { client.deleteConfigurationSetting(capture(keySlot), capture(labelSlot)) } returns mockk<ConfigurationSetting>()

            val params = project.objects.newInstance<DeleteConfigurationSettingAction.Parameters>()
            params.service.set(service)
            params.key.set("app.config")
            params.label.set("staging")

            val action = object : DeleteConfigurationSettingAction() {
                override fun getParameters() = params
            }
            action.execute()

            keySlot.captured shouldBe "app.config"
            labelSlot.captured shouldBe "staging"
        }
    }
}
