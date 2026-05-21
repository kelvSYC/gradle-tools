package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.resourcemanager.appconfiguration.AppConfigurationManager
import com.azure.resourcemanager.appconfiguration.models.ConfigurationStore
import com.azure.resourcemanager.appconfiguration.models.ConfigurationStores
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.verify
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class CreateConfigurationStoreActionSpec : FunSpec() {
    init {
        test("execute - creates store with default Free sku when sku not set") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager

            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores

            // Mock the fluent builder chain
            val blank = mockk<ConfigurationStore.DefinitionStages.Blank>()
            val withLocation = mockk<ConfigurationStore.DefinitionStages.WithResourceGroup>()
            val withResourceGroup = mockk<ConfigurationStore.DefinitionStages.WithSku>()
            val withSku = mockk<ConfigurationStore.DefinitionStages.WithCreate>()
            val store = mockk<ConfigurationStore>()

            every { stores.define(any()) } returns blank
            every { blank.withRegion(any<String>()) } returns withLocation
            every { withLocation.withExistingResourceGroup(any()) } returns withResourceGroup
            every { withResourceGroup.withSku(any()) } returns withSku
            every { withSku.create() } returns store

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("my-rg")
            }

            val params = project.objects.newInstance<CreateConfigurationStoreAction.Parameters>()
            params.service.set(service)
            params.storeName.set("test-store")
            params.location.set("eastus")

            val action = object : CreateConfigurationStoreAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { blank.withRegion("eastus") }
            verify { withLocation.withExistingResourceGroup("my-rg") }
            verify { withSku.create() }
        }

        test("execute - creates store with specified sku") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager

            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores

            val blank = mockk<ConfigurationStore.DefinitionStages.Blank>()
            val withLocation = mockk<ConfigurationStore.DefinitionStages.WithResourceGroup>()
            val withResourceGroup = mockk<ConfigurationStore.DefinitionStages.WithSku>()
            val withSku = mockk<ConfigurationStore.DefinitionStages.WithCreate>()
            val store = mockk<ConfigurationStore>()

            val skuSlot = slot<com.azure.resourcemanager.appconfiguration.models.Sku>()

            every { stores.define(any()) } returns blank
            every { blank.withRegion(any<String>()) } returns withLocation
            every { withLocation.withExistingResourceGroup(any()) } returns withResourceGroup
            every { withResourceGroup.withSku(capture(skuSlot)) } returns withSku
            every { withSku.create() } returns store

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("my-rg")
            }

            val params = project.objects.newInstance<CreateConfigurationStoreAction.Parameters>()
            params.service.set(service)
            params.storeName.set("standard-store")
            params.location.set("westus")
            params.sku.set("Standard")

            val action = object : CreateConfigurationStoreAction() {
                override fun getParameters() = params
            }
            action.execute()

            skuSlot.captured.name() shouldBe "Standard"
        }

        test("execute - uses resource group from service") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager

            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores

            val blank = mockk<ConfigurationStore.DefinitionStages.Blank>()
            val withLocation = mockk<ConfigurationStore.DefinitionStages.WithResourceGroup>()
            val withResourceGroup = mockk<ConfigurationStore.DefinitionStages.WithSku>()
            val withSku = mockk<ConfigurationStore.DefinitionStages.WithCreate>()
            val store = mockk<ConfigurationStore>()

            val rgSlot = slot<String>()

            every { stores.define(any()) } returns blank
            every { blank.withRegion(any<String>()) } returns withLocation
            every { withLocation.withExistingResourceGroup(capture(rgSlot)) } returns withResourceGroup
            every { withResourceGroup.withSku(any()) } returns withSku
            every { withSku.create() } returns store

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("service-rg")
            }

            val params = project.objects.newInstance<CreateConfigurationStoreAction.Parameters>()
            params.service.set(service)
            params.storeName.set("app-store")
            params.location.set("northeurope")

            val action = object : CreateConfigurationStoreAction() {
                override fun getParameters() = params
            }
            action.execute()

            rgSlot.captured shouldBe "service-rg"
        }
    }
}

