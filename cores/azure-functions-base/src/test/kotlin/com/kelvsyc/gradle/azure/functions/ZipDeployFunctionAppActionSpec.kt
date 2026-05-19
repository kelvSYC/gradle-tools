package com.kelvsyc.gradle.azure.functions

import com.azure.resourcemanager.appservice.AppServiceManager
import com.azure.resourcemanager.appservice.models.FunctionApp
import com.azure.resourcemanager.appservice.models.FunctionApps
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.string.shouldContain
import io.mockk.every
import io.mockk.justRun
import io.mockk.mockk
import io.mockk.mockkConstructor
import io.mockk.slot
import io.mockk.unmockkAll
import okhttp3.Call
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.Response
import org.gradle.kotlin.dsl.newInstance
import org.gradle.testfixtures.ProjectBuilder
import java.io.File
import java.lang.reflect.Method as ReflectMethod

class ZipDeployFunctionAppActionSpec : FunSpec() {
    init {
        afterTest { unmockkAll() }

        test("execute - posts zip to kudu endpoint with basic auth") {
            mockkConstructor(OkHttpClient::class)
            val project = ProjectBuilder.builder().build()

            // Create a mock publishing profile object with the expected methods
            val mockProfile = object {
                fun publishingUserName(): String = "test-user"
                fun publishingPassword(): String = "test-password"
            }

            val mockManager = mockk<AppServiceManager>()
            val mockFunctionApps = mockk<FunctionApps>()
            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()
            val mockApp = mockk<FunctionApp>(relaxed = true)

            // Create a mock method that returns our profile list
            val mockMethod = mockk<ReflectMethod>()
            every { mockMethod.name } returns "getPublishingProfiles"
            every { mockMethod.invoke(mockApp) } returns listOf(mockProfile)

            every { mockApp.javaClass.getDeclaredMethod("getPublishingProfiles") } returns mockMethod
            every { mockManager.functionApps() } returns mockFunctionApps
            every { mockFunctionApps.getByResourceGroup("test-rg", "my-app") } returns mockApp

            justRun { mockResponse.close() }
            every { mockCall.execute() } returns mockResponse
            val requestSlot = slot<Request>()
            every { anyConstructed<OkHttpClient>().newCall(capture(requestSlot)) } returns mockCall

            val mockService = mockk<FunctionAppClientBuildService>(relaxed = true)
            every { mockService.getClient() } returns mockManager
            val mockServiceParams = mockk<FunctionAppClientBuildService.Params>(relaxed = true)
            every { mockServiceParams.resourceGroup.get() } returns "test-rg"
            every { mockService.parameters } returns mockServiceParams

            val zipFile = File.createTempFile("deploy", ".zip").also { it.deleteOnExit() }
            val params = project.objects.newInstance<ZipDeployFunctionAppAction.Parameters>()
            params.appService.set(mockService)
            params.appName.set("my-app")
            params.zipFile.set(zipFile)

            val action = object : ZipDeployFunctionAppAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.url.toString() shouldContain "my-app.scm.azurewebsites.net"
            requestSlot.captured.url.toString() shouldContain "zipdeploy"
            requestSlot.captured.url.scheme shouldBe "https"
            requestSlot.captured.header("Authorization") shouldBe
                okhttp3.Credentials.basic("test-user", "test-password")
        }
    }
}
