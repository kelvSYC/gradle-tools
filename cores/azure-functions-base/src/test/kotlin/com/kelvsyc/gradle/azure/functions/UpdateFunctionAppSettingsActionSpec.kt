package com.kelvsyc.gradle.azure.functions

import com.azure.resourcemanager.appservice.AppServiceManager
import com.azure.resourcemanager.appservice.models.FunctionApp
import com.azure.resourcemanager.appservice.models.FunctionApps
import com.kelvsyc.gradle.clients.CredentialReference
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.maps.shouldContainAll
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class UpdateFunctionAppSettingsActionSpec : FunSpec() {
    init {
        afterTest { unmockkAll() }

        test("execute - merges plain and sensitive settings into a single ARM update call") {
            val project = ProjectBuilder.builder().build()
            val mockManager = mockk<AppServiceManager>()
            val mockFunctionApps = mockk<FunctionApps>()
            val mockApp = mockk<FunctionApp>()
            val mockUpdate = mockk<FunctionApp.Update>()

            MockFunctionAppClientBuildService.mockClient = mockManager
            every { mockManager.functionApps() } returns mockFunctionApps
            every { mockFunctionApps.getByResourceGroup("test-rg", "my-app") } returns mockApp
            val settingsSlot = slot<Map<String, String>>()
            every { mockApp.update() } returns mockUpdate
            every { mockUpdate.withAppSettings(capture(settingsSlot)) } returns mockUpdate
            every { mockUpdate.apply() } returns mockApp

            val service = project.gradle.sharedServices.registerIfAbsent(
                "functions-service",
                MockFunctionAppClientBuildService::class
            ) { spec -> spec.parameters.resourceGroup.set("test-rg") }

            val params = project.objects.newInstance<UpdateFunctionAppSettingsAction.Parameters>()
            params.appService.set(service)
            params.appName.set("my-app")
            params.settings.put("LOG_LEVEL", "info")
            params.settings.put("REGION", "eastus")
            params.sensitiveSettings.put("DB_CONNECTION_STRING", CredentialReference.Literal("secret-conn"))

            val action = object : UpdateFunctionAppSettingsAction() {
                override fun getParameters() = params
            }
            action.execute()

            settingsSlot.captured shouldContainAll mapOf(
                "LOG_LEVEL" to "info",
                "REGION" to "eastus",
                "DB_CONNECTION_STRING" to "secret-conn",
            )
        }
    }
}
