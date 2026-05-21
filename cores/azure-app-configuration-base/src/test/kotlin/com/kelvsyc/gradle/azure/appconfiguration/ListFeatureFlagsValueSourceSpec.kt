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

class ListFeatureFlagsValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns feature name to enabled map (placeholder until JSON parsing added)") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val flag1 = mockk<ConfigurationSetting>()
            every { flag1.key } returns ".appconfig.featureflag/feature-a"
            every { flag1.contentType } returns "application/vnd.microsoft.appconfig.featureflag+json"

            val flag2 = mockk<ConfigurationSetting>()
            every { flag2.key } returns ".appconfig.featureflag/feature-b"
            every { flag2.contentType } returns "application/vnd.microsoft.appconfig.featureflag+json"

            every { client.listConfigurationSettings(any()) } returns
                mockk { every { iterator() } returns listOf(flag1, flag2).toMutableList().iterator() }

            val provider = project.providers.ofKt(ListFeatureFlagsValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            // Returns false for all until JSON parsing is implemented
            result shouldContainExactly mapOf(
                "feature-a" to false,
                "feature-b" to false
            )
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
                mockk { every { iterator() } returns listOf<ConfigurationSetting>().toMutableList().iterator() }

            val provider = project.providers.ofKt(ListFeatureFlagsValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result.shouldBeEmpty()
        }

        test("obtain - uses feature flag key prefix in selector") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<ConfigurationClient>()
            MockAppConfigurationClientBuildService.mockClient = client
            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig",
                MockAppConfigurationClientBuildService::class
            )

            val flag = mockk<ConfigurationSetting>()
            every { flag.key } returns ".appconfig.featureflag/test-flag"
            every { flag.contentType } returns "application/vnd.microsoft.appconfig.featureflag+json"

            every { client.listConfigurationSettings(any()) } returns
                mockk { every { iterator() } returns listOf(flag).toMutableList().iterator() }

            val provider = project.providers.ofKt(ListFeatureFlagsValueSource::class) {
                parameters.service.set(service)
            }
            val result = provider.get()

            result shouldContainExactly mapOf("test-flag" to false)
        }
    }
}

