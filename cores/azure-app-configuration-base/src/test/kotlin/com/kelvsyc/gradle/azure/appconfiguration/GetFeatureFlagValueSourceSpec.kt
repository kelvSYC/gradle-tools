package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.exception.ResourceNotFoundException
import com.azure.data.appconfiguration.ConfigurationClient
import com.azure.data.appconfiguration.models.FeatureFlagConfigurationSetting
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetFeatureFlagValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns true for enabled flag") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val flag = FeatureFlagConfigurationSetting("new-ui", true)
            every { client.getConfigurationSetting(any(), null) } returns flag

            val provider = project.providers.ofKt(GetFeatureFlagValueSource::class) {
                parameters.service.set(service)
                parameters.featureName.set("new-ui")
            }

            provider.orNull shouldBe true
        }

        test("obtain - returns false for disabled flag") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val flag = FeatureFlagConfigurationSetting("old-ui", false)
            every { client.getConfigurationSetting(any(), null) } returns flag

            val provider = project.providers.ofKt(GetFeatureFlagValueSource::class) {
                parameters.service.set(service)
                parameters.featureName.set("old-ui")
            }

            provider.orNull shouldBe false
        }

        test("obtain - returns null when not found") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            every { client.getConfigurationSetting(any(), null) } throws
                ResourceNotFoundException("not found", null)

            val provider = project.providers.ofKt(GetFeatureFlagValueSource::class) {
                parameters.service.set(service)
                parameters.featureName.set("missing-flag")
            }

            provider.orNull.shouldBeNull()
        }

        test("obtain - uses correct key prefix") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val keySlot = slot<String>()
            val flag = FeatureFlagConfigurationSetting("my-feature", true)
            every { client.getConfigurationSetting(capture(keySlot), null) } returns flag

            val provider = project.providers.ofKt(GetFeatureFlagValueSource::class) {
                parameters.service.set(service)
                parameters.featureName.set("my-feature")
            }
            provider.orNull

            keySlot.captured shouldBe ".appconfig.featureflag/my-feature"
        }

        test("obtain - passes label when set") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )
            val labelSlot = slot<String>()
            val flag = FeatureFlagConfigurationSetting("my-feature", true)
            every { client.getConfigurationSetting(any(), capture(labelSlot)) } returns flag

            val provider = project.providers.ofKt(GetFeatureFlagValueSource::class) {
                parameters.service.set(service)
                parameters.featureName.set("my-feature")
                parameters.label.set("prod")
            }
            provider.orNull

            labelSlot.captured shouldBe "prod"
        }
    }
}
