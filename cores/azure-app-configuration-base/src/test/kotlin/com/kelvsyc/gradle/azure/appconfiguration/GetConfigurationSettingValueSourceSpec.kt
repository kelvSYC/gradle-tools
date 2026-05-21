package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.exception.ResourceNotFoundException
import com.azure.data.appconfiguration.ConfigurationClient
import com.azure.data.appconfiguration.models.ConfigurationSetting
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetConfigurationSettingValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns value for plain setting") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val keySlot = slot<String>()
            val setting = mockk<ConfigurationSetting>()
            every { setting.value } returns "hello"
            every { setting.contentType } returns null
            every { client.getConfigurationSetting(capture(keySlot), any()) } returns setting

            val provider = project.providers.ofKt(GetConfigurationSettingValueSource::class) {
                parameters.service.set(service)
                parameters.key.set("app.name")
            }
            val result = provider.get()

            result shouldBe "hello"
            keySlot.captured shouldBe "app.name"
        }

        test("obtain - returns empty string for Key Vault reference") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val kvSetting = mockk<ConfigurationSetting>()
            every { kvSetting.contentType } returns "application/vnd.microsoft.appconfig.keyvaultref+json"
            every { client.getConfigurationSetting(any(), any()) } returns kvSetting

            val provider = project.providers.ofKt(GetConfigurationSettingValueSource::class) {
                parameters.service.set(service)
                parameters.key.set("kv-ref")
            }
            val result = provider.get()

            result shouldBe ""
        }

        test("obtain - returns empty string when setting not found") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            every { client.getConfigurationSetting(any(), any()) } throws
                ResourceNotFoundException("not found", null)

            val provider = project.providers.ofKt(GetConfigurationSettingValueSource::class) {
                parameters.service.set(service)
                parameters.key.set("missing-key")
            }
            val result = provider.get()

            result shouldBe ""
        }

        test("obtain - passes label when set") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val keySlot = slot<String>()
            val labelSlot = slot<String>()
            val setting = mockk<ConfigurationSetting>()
            every { setting.value } returns "prod-value"
            every { setting.contentType } returns null
            every { client.getConfigurationSetting(capture(keySlot), capture(labelSlot)) } returns setting

            val provider = project.providers.ofKt(GetConfigurationSettingValueSource::class) {
                parameters.service.set(service)
                parameters.key.set("app.config")
                parameters.label.set("prod")
            }
            val result = provider.get()

            result shouldBe "prod-value"
            keySlot.captured shouldBe "app.config"
            labelSlot.captured shouldBe "prod"
        }
    }
}


