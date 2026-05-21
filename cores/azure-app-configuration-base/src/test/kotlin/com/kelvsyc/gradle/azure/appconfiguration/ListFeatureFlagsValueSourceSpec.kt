package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.data.appconfiguration.ConfigurationClient
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListFeatureFlagsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns feature name to enabled map") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val flag1 = FeatureFlagConfigurationSetting("feature-a", true)
            val flag2 = FeatureFlagConfigurationSetting("feature-b", false)
            every { client.listConfigurationSettings(any()) } returns
                mockk { every { iterator() } returns mutableListOf(flag1, flag2).iterator() }

            val provider = project.providers.ofKt(ListFeatureFlagsValueSource::class) {
                parameters.service.set(service)
            }

            provider.get() shouldContainExactly mapOf("feature-a" to true, "feature-b" to false)
        }

        test("obtain - returns empty map when no flags") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            every { client.listConfigurationSettings(any()) } returns
                mockk { every { iterator() } returns mutableListOf<FeatureFlagConfigurationSetting>().iterator() }

            val provider = project.providers.ofKt(ListFeatureFlagsValueSource::class) {
                parameters.service.set(service)
            }

            provider.get().shouldBeEmpty()
        }

        test("obtain - returns flag names without prefix") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val flag = FeatureFlagConfigurationSetting("test-flag", true)
            every { client.listConfigurationSettings(any()) } returns
                mockk { every { iterator() } returns mutableListOf(flag).iterator() }

            val provider = project.providers.ofKt(ListFeatureFlagsValueSource::class) {
                parameters.service.set(service)
            }

            provider.get() shouldContainExactly mapOf("test-flag" to true)
        }
    }
}
