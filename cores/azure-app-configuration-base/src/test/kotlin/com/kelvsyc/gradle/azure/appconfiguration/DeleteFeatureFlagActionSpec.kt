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

class DeleteFeatureFlagActionSpec : FunSpec() {
    init {
        test("execute - deletes flag with correct key prefix") {
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

            val params = project.objects.newInstance<DeleteFeatureFlagAction.Parameters>()
            params.service.set(service)
            params.featureName.set("my-flag")

            val action = object : DeleteFeatureFlagAction() {
                override fun getParameters() = params
            }
            action.execute()

            val expectedKey = FeatureFlagConfigurationSetting.KEY_PREFIX + "my-flag"
            keySlot.captured shouldBe expectedKey
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

            val params = project.objects.newInstance<DeleteFeatureFlagAction.Parameters>()
            params.service.set(service)
            params.featureName.set("experimental-ui")
            params.label.set("prod")

            val action = object : DeleteFeatureFlagAction() {
                override fun getParameters() = params
            }
            action.execute()

            val expectedKey = FeatureFlagConfigurationSetting.KEY_PREFIX + "experimental-ui"
            keySlot.captured shouldBe expectedKey
            labelSlot.captured shouldBe "prod"
        }
    }
}
