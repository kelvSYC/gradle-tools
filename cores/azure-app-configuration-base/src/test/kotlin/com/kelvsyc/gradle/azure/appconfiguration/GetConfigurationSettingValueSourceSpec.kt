package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.exception.ResourceNotFoundException
import com.azure.data.appconfiguration.ConfigurationClient
import com.azure.data.appconfiguration.models.ConfigurationSetting
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
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
            val setting = ConfigurationSetting().setKey("app.name").setValue("hello")
            every { client.getConfigurationSetting(capture(keySlot), null) } returns setting

            val provider = project.providers.ofKt(GetConfigurationSettingValueSource::class) {
                parameters.service.set(service)
                parameters.key.set("app.name")
            }

            provider.orNull shouldBe "hello"
            keySlot.captured shouldBe "app.name"
        }

        test("obtain - returns null for Key Vault reference") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val kvSetting = ConfigurationSetting()
                .setKey("kv-ref")
                .setContentType("application/vnd.microsoft.appconfig.keyvaultref+json;charset=utf-8")
            every { client.getConfigurationSetting(any(), null) } returns kvSetting

            val provider = project.providers.ofKt(GetConfigurationSettingValueSource::class) {
                parameters.service.set(service)
                parameters.key.set("kv-ref")
            }

            provider.orNull.shouldBeNull()
        }

        test("obtain - returns null when setting not found") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            every { client.getConfigurationSetting(any(), null) } throws
                ResourceNotFoundException("not found", null)

            val provider = project.providers.ofKt(GetConfigurationSettingValueSource::class) {
                parameters.service.set(service)
                parameters.key.set("missing-key")
            }

            provider.orNull.shouldBeNull()
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
            val setting = ConfigurationSetting().setKey("app.config").setValue("prod-value")
            every { client.getConfigurationSetting(capture(keySlot), capture(labelSlot)) } returns setting

            val provider = project.providers.ofKt(GetConfigurationSettingValueSource::class) {
                parameters.service.set(service)
                parameters.key.set("app.config")
                parameters.label.set("prod")
            }

            provider.orNull shouldBe "prod-value"
            keySlot.captured shouldBe "app.config"
            labelSlot.captured shouldBe "prod"
        }
    }
}
