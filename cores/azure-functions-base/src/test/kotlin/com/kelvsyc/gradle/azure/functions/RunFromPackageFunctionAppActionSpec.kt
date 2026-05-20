package com.kelvsyc.gradle.azure.functions

import com.azure.resourcemanager.appservice.AppServiceManager
import com.azure.resourcemanager.appservice.models.FunctionApp
import com.azure.resourcemanager.appservice.models.FunctionApps
import com.kelvsyc.gradle.clients.CredentialReference
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import io.mockk.slot
import io.mockk.unmockkAll
import org.gradle.kotlin.dsl.newInstance
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class RunFromPackageFunctionAppActionSpec : FunSpec() {
    init {
        afterTest { unmockkAll() }

        test("execute - plainUrl sets WEBSITE_RUN_FROM_PACKAGE to the provided URL") {
            val project = ProjectBuilder.builder().build()
            val mockManager = mockk<AppServiceManager>()
            val mockFunctionApps = mockk<FunctionApps>()
            val mockApp = mockk<FunctionApp>()
            val mockUpdate = mockk<FunctionApp.Update>()

            MockFunctionAppClientBuildService.mockClient = mockManager
            every { mockManager.functionApps() } returns mockFunctionApps
            every { mockFunctionApps.getByResourceGroup("test-rg", "my-app") } returns mockApp
            val settingKeySlot = slot<String>()
            val settingValueSlot = slot<String>()
            every { mockApp.update() } returns mockUpdate
            every { mockUpdate.withAppSetting(capture(settingKeySlot), capture(settingValueSlot)) } returns mockUpdate
            every { mockUpdate.apply() } returns mockApp

            val service = project.gradle.sharedServices.registerIfAbsent(
                "functions-service",
                MockFunctionAppClientBuildService::class
            ) { spec -> spec.parameters.resourceGroup.set("test-rg") }

            val params = project.objects.newInstance<RunFromPackageFunctionAppAction.Parameters>()
            params.appService.set(service)
            params.appName.set("my-app")
            params.plainUrl("https://mystorage.blob.core.windows.net/deploys/app.zip")

            val action = object : RunFromPackageFunctionAppAction() {
                override fun getParameters() = params
            }
            action.execute()

            settingKeySlot.captured shouldBe "WEBSITE_RUN_FROM_PACKAGE"
            settingValueSlot.captured shouldBe "https://mystorage.blob.core.windows.net/deploys/app.zip"
        }

        test("execute - sasUrl resolves CredentialReference and sets WEBSITE_RUN_FROM_PACKAGE") {
            val project = ProjectBuilder.builder().build()
            val mockManager = mockk<AppServiceManager>()
            val mockFunctionApps = mockk<FunctionApps>()
            val mockApp = mockk<FunctionApp>()
            val mockUpdate = mockk<FunctionApp.Update>()

            MockFunctionAppClientBuildService.mockClient = mockManager
            every { mockManager.functionApps() } returns mockFunctionApps
            every { mockFunctionApps.getByResourceGroup("test-rg", "my-app") } returns mockApp
            val settingValueSlot = slot<String>()
            every { mockApp.update() } returns mockUpdate
            every { mockUpdate.withAppSetting(any(), capture(settingValueSlot)) } returns mockUpdate
            every { mockUpdate.apply() } returns mockApp

            val service = project.gradle.sharedServices.registerIfAbsent(
                "functions-service2",
                MockFunctionAppClientBuildService::class
            ) { spec -> spec.parameters.resourceGroup.set("test-rg") }

            val params = project.objects.newInstance<RunFromPackageFunctionAppAction.Parameters>()
            params.appService.set(service)
            params.appName.set("my-app")
            params.sasUrl(CredentialReference.Literal("https://mystorage.blob.core.windows.net/deploys/app.zip?sv=2021-06-08&sig=abc"))

            val action = object : RunFromPackageFunctionAppAction() {
                override fun getParameters() = params
            }
            action.execute()

            settingValueSlot.captured shouldBe "https://mystorage.blob.core.windows.net/deploys/app.zip?sv=2021-06-08&sig=abc"
        }
    }
}
