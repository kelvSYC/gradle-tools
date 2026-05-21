package com.kelvsyc.gradle.azure.appconfiguration

import com.azure.resourcemanager.appconfiguration.AppConfigurationManager
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

class DeleteConfigurationStoreActionSpec : FunSpec() {
    init {
        test("execute - deletes store by name") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager

            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores
            every { stores.deleteByResourceGroup(any(), any()) } returns Unit

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("my-rg")
            }

            val params = project.objects.newInstance<DeleteConfigurationStoreAction.Parameters>()
            params.service.set(service)
            params.storeName.set("test-store")

            val action = object : DeleteConfigurationStoreAction() {
                override fun getParameters() = params
            }
            action.execute()

            verify { stores.deleteByResourceGroup("my-rg", "test-store") }
        }

        test("execute - uses resource group from service") {
            val project = ProjectBuilder.builder().build()
            val manager = mockk<AppConfigurationManager>()
            MockAppConfigurationManagerBuildService.mockClient = manager

            val stores = mockk<ConfigurationStores>()
            every { manager.configurationStores() } returns stores

            val rgSlot = slot<String>()
            val nameSlot = slot<String>()
            every { stores.deleteByResourceGroup(capture(rgSlot), capture(nameSlot)) } returns Unit

            val service = project.gradle.sharedServices.registerIfAbsent(
                "appconfig-manager",
                MockAppConfigurationManagerBuildService::class
            ) {
                it.parameters.resourceGroup.set("custom-rg")
            }

            val params = project.objects.newInstance<DeleteConfigurationStoreAction.Parameters>()
            params.service.set(service)
            params.storeName.set("store-to-delete")

            val action = object : DeleteConfigurationStoreAction() {
                override fun getParameters() = params
            }
            action.execute()

            rgSlot.captured shouldBe "custom-rg"
            nameSlot.captured shouldBe "store-to-delete"
        }
    }
}

