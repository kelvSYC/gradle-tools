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
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import java.io.File

class ZipDeployFunctionAppActionSpec : FunSpec() {
    init {
        afterTest { unmockkAll() }

        test("execute - posts zip to kudu endpoint with basic auth") {
            mockkConstructor(OkHttpClient::class)
            val project = ProjectBuilder.builder().build()

            val mockManager = mockk<AppServiceManager>()
            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()
            val mockServiceProvider = mockk<FunctionAppClientBuildService>()

            justRun { mockResponse.close() }
            every { mockCall.execute() } returns mockResponse
            val requestSlot = slot<Request>()
            every { anyConstructed<OkHttpClient>().newCall(capture(requestSlot)) } returns mockCall
            every { mockServiceProvider.getClient() } returns mockManager
            every { mockServiceProvider.parameters.resourceGroup.get() } returns "test-rg"

            val zipFile = File.createTempFile("deploy", ".zip").also { it.deleteOnExit() }
            val params = project.objects.newInstance<ZipDeployFunctionAppAction.Parameters>()
            params.appService.set(mockServiceProvider)
            params.appName.set("my-app")
            params.zipFile.set(zipFile)

            val action = object : ZipDeployFunctionAppAction() {
                override fun getParameters() = params
                override fun retrievePublishingCredentials(
                    manager: AppServiceManager,
                    resourceGroup: String,
                    appName: String,
                ) = PublishingCredentials(
                    publishingUserName = "test-user",
                    publishingPassword = "test-password"
                )
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
