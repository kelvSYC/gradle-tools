package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.ConfigurationClient
import com.azure.data.appconfiguration.models.ConfigurationSetting
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class SetFeatureFlagActionSpec : FunSpec() {
    init {
        test("execute - sets enabled flag") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val settingSlot = slot<ConfigurationSetting>()
            every { client.setConfigurationSetting(capture(settingSlot)) } returns mockk()

            val params = project.objects.newInstance<SetFeatureFlagAction.Parameters>()
            params.service.set(service)
            params.featureName.set("new-dashboard")
            params.enabled.set(true)

            val action = object : SetFeatureFlagAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = settingSlot.captured as FeatureFlagConfigurationSetting
            captured.isEnabled shouldBe true
        }

        test("execute - sets disabled flag") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val settingSlot = slot<ConfigurationSetting>()
            every { client.setConfigurationSetting(capture(settingSlot)) } returns mockk()

            val params = project.objects.newInstance<SetFeatureFlagAction.Parameters>()
            params.service.set(service)
            params.featureName.set("legacy-api")
            params.enabled.set(false)

            val action = object : SetFeatureFlagAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = settingSlot.captured as FeatureFlagConfigurationSetting
            captured.isEnabled shouldBe false
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

            val params = project.objects.newInstance<SetFeatureFlagAction.Parameters>()
            params.service.set(service)
            params.featureName.set("canary-deploy")
            params.enabled.set(true)
            params.label.set("beta")

            val action = object : SetFeatureFlagAction() {
                override fun getParameters() = params
            }
            action.execute()

            settingSlot.captured.label shouldBe "beta"
        }

        test("execute - sets description when present") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val settingSlot = slot<ConfigurationSetting>()
            every { client.setConfigurationSetting(capture(settingSlot)) } returns mockk()

            val params = project.objects.newInstance<SetFeatureFlagAction.Parameters>()
            params.service.set(service)
            params.featureName.set("analytics")
            params.enabled.set(true)
            params.description.set("Enable enhanced analytics dashboard")

            val action = object : SetFeatureFlagAction() {
                override fun getParameters() = params
            }
            action.execute()

            val captured = settingSlot.captured as FeatureFlagConfigurationSetting
            captured.description shouldBe "Enable enhanced analytics dashboard"
        }
    }
}
