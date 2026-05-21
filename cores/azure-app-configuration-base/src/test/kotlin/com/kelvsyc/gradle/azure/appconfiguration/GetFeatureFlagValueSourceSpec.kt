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

            val flag = mockk<ConfigurationSetting>()
            every { flag.contentType } returns "application/vnd.microsoft.appconfig.featureflag+json"
            every { flag.value } returns """{"enabled":true}"""
            every { client.getConfigurationSetting(any(), any()) } returns flag

            val provider = project.providers.ofKt(GetFeatureFlagValueSource::class) {
                parameters.service.set(service)
                parameters.featureName.set("new-ui")
            }
            val result = provider.get()

            result shouldBe true
        }

        test("obtain - returns false for disabled flag") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val flag = mockk<ConfigurationSetting>()
            every { flag.contentType } returns "application/vnd.microsoft.appconfig.featureflag+json"
            every { flag.value } returns """{"enabled":false}"""
            every { client.getConfigurationSetting(any(), any()) } returns flag

            val provider = project.providers.ofKt(GetFeatureFlagValueSource::class) {
                parameters.service.set(service)
                parameters.featureName.set("old-ui")
            }
            val result = provider.get()

            result shouldBe false
        }

        test("obtain - returns false when not found") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            every { client.getConfigurationSetting(any(), any()) } throws
                ResourceNotFoundException("not found", null)

            val provider = project.providers.ofKt(GetFeatureFlagValueSource::class) {
                parameters.service.set(service)
                parameters.featureName.set("missing-flag")
            }
            val result = provider.get()

            result shouldBe false
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
            val flag = mockk<ConfigurationSetting>()
            every { flag.contentType } returns "application/vnd.microsoft.appconfig.featureflag+json"
            every { flag.value } returns """{"enabled":true}"""
            every { client.getConfigurationSetting(capture(keySlot), any()) } returns flag

            val provider = project.providers.ofKt(GetFeatureFlagValueSource::class) {
                parameters.service.set(service)
                parameters.featureName.set("my-feature")
            }
            provider.get()

            keySlot.captured shouldBe ".appconfig.featureflag/my-feature"
        }
    }
}



