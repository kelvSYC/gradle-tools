package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.ConfigurationClient
import com.azure.data.appconfiguration.models.ConfigurationSetting
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListConfigurationSettingsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns key-value map from settings") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val setting1 = mockk<ConfigurationSetting>()
            every { setting1.key } returns "db.host"
            every { setting1.value } returns "localhost"
            every { setting1.contentType } returns null

            val setting2 = mockk<ConfigurationSetting>()
            every { setting2.key } returns "db.port"
            every { setting2.value } returns "5432"
            every { setting2.contentType } returns null

            every { client.listConfigurationSettings(any()) } returns
                mockk { every { iterator() } returns listOf(setting1, setting2).toMutableList().iterator() }

            val provider = project.providers.ofKt(ListConfigurationSettingsValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result shouldContainExactly mapOf(
                "db.host" to "localhost",
                "db.port" to "5432"
            )
        }

        test("obtain - skips Key Vault reference entries") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val plainSetting = mockk<ConfigurationSetting>()
            every { plainSetting.key } returns "app.name"
            every { plainSetting.value } returns "myapp"
            every { plainSetting.contentType } returns null

            val kvSetting = mockk<ConfigurationSetting>()
            every { kvSetting.key } returns "kv-ref"
            every { kvSetting.value } returns "{...}"
            every { kvSetting.contentType } returns "application/vnd.microsoft.appconfig.keyvaultref+json"

            every { client.listConfigurationSettings(any()) } returns
                mockk { every { iterator() } returns listOf(plainSetting, kvSetting).toMutableList().iterator() }

            val provider = project.providers.ofKt(ListConfigurationSettingsValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result shouldContainExactly mapOf("app.name" to "myapp")
        }

        test("obtain - returns empty map when no settings") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            every { client.listConfigurationSettings(any()) } returns
                mockk { every { iterator() } returns listOf<ConfigurationSetting>().toMutableList().iterator() }

            val provider = project.providers.ofKt(ListConfigurationSettingsValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result.shouldBeEmpty()
        }
    }
}

