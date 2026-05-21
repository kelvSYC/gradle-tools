package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.core.management.exception.ManagementException
import com.azure.resourcemanager.appconfiguration.AppConfigurationManager
import com.azure.resourcemanager.appconfiguration.models.ConfigurationStore
import com.azure.resourcemanager.appconfiguration.models.ConfigurationStores
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetConfigurationStoreValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns endpoint URL for existing store") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager
            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores

            val store = mockk<ConfigurationStore>()
            every { store.endpoint() } returns "https://my-store.azconfig.io"
            every { stores.getByResourceGroup("rg", "my-store") } returns store

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("rg")
            }

            val provider = project.providers.ofKt(GetConfigurationStoreValueSource::class) {
                parameters.service.set(service)
                parameters.storeName.set("my-store")
            }

            provider.orNull shouldBe "https://my-store.azconfig.io"
        }

        test("obtain - returns null when store not found") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager
            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores
            every { stores.getByResourceGroup(any(), any()) } throws
                ManagementException("not found", null)

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("rg")
            }

            val provider = project.providers.ofKt(GetConfigurationStoreValueSource::class) {
                parameters.service.set(service)
                parameters.storeName.set("missing-store")
            }

            provider.orNull.shouldBeNull()
        }

        test("obtain - returns null when endpoint is not available") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager
            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores

            val store = mockk<ConfigurationStore>()
            every { store.endpoint() } returns null
            every { stores.getByResourceGroup("rg", "provisioning-store") } returns store

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("rg")
            }

            val provider = project.providers.ofKt(GetConfigurationStoreValueSource::class) {
                parameters.service.set(service)
                parameters.storeName.set("provisioning-store")
            }

            provider.orNull.shouldBeNull()
        }

        test("obtain - uses resource group from service parameters") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager
            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores

            val rgSlot = slot<String>()
            val store = mockk<ConfigurationStore>()
            every { store.endpoint() } returns "https://test-store.azconfig.io"
            every { stores.getByResourceGroup(capture(rgSlot), any()) } returns store

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("my-resource-group")
            }

            val provider = project.providers.ofKt(GetConfigurationStoreValueSource::class) {
                parameters.service.set(service)
                parameters.storeName.set("test-store")
            }
            provider.orNull

            rgSlot.captured shouldBe "my-resource-group"
        }
    }
}
