package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.resourcemanager.appconfiguration.AppConfigurationManager
import com.azure.resourcemanager.appconfiguration.models.ConfigurationStore
import com.azure.resourcemanager.appconfiguration.models.ConfigurationStores
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldBeEmpty
import io.kotest.matchers.maps.shouldContainExactly
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class ListConfigurationStoresValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns store name to endpoint map") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager
            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores

            val store1 = mockk<ConfigurationStore>()
            every { store1.name() } returns "store-1"
            every { store1.endpoint() } returns "https://store-1.azconfig.io"

            val store2 = mockk<ConfigurationStore>()
            every { store2.name() } returns "store-2"
            every { store2.endpoint() } returns "https://store-2.azconfig.io"

            every { stores.listByResourceGroup(any()) } returns
                mockk { every { iterator() } returns mutableListOf(store1, store2).iterator() }

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("rg")
            }

            val provider = project.providers.ofKt(ListConfigurationStoresValueSource::class) {
                parameters.service.set(service)
            }

            provider.get() shouldContainExactly mapOf(
                "store-1" to "https://store-1.azconfig.io",
                "store-2" to "https://store-2.azconfig.io"
            )
        }

        test("obtain - returns empty map when no stores") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager
            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores

            every { stores.listByResourceGroup(any()) } returns
                mockk { every { iterator() } returns mutableListOf<ConfigurationStore>().iterator() }

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("rg")
            }

            val provider = project.providers.ofKt(ListConfigurationStoresValueSource::class) {
                parameters.service.set(service)
            }

            provider.get().shouldBeEmpty()
        }

        test("obtain - skips stores with null endpoints") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager
            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores

            val store1 = mockk<ConfigurationStore>()
            every { store1.name() } returns "ready-store"
            every { store1.endpoint() } returns "https://ready-store.azconfig.io"

            val store2 = mockk<ConfigurationStore>()
            every { store2.name() } returns "provisioning-store"
            every { store2.endpoint() } returns null

            every { stores.listByResourceGroup(any()) } returns
                mockk { every { iterator() } returns mutableListOf(store1, store2).iterator() }

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("rg")
            }

            val provider = project.providers.ofKt(ListConfigurationStoresValueSource::class) {
                parameters.service.set(service)
            }

            provider.get() shouldContainExactly mapOf("ready-store" to "https://ready-store.azconfig.io")
        }
    }
}
