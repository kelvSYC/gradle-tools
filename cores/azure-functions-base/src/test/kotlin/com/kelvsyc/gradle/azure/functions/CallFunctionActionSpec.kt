package com.kelvsyc.gradle.azure.functions

import com.kelvsyc.gradle.clients.CredentialReference
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
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

class CallFunctionActionSpec : FunSpec() {
    init {
        afterTest { unmockkAll() }

        test("execute - anonymous mode sends POST with no auth headers") {
            mockkConstructor(OkHttpClient::class)
            val project = ProjectBuilder.builder().build()
            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()
            justRun { mockResponse.close() }
            every { mockCall.execute() } returns mockResponse
            val requestSlot = slot<Request>()
            every { anyConstructed<OkHttpClient>().newCall(capture(requestSlot)) } returns mockCall

            val params = project.objects.newInstance<CallFunctionAction.Parameters>()
            params.uri.set("https://myapp.azurewebsites.net/api/myfunction")
            params.anonymous()

            val action = object : CallFunctionAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.method shouldBe "POST"
            requestSlot.captured.header("x-functions-key") shouldBe null
            requestSlot.captured.header("Authorization") shouldBe null
        }

        test("execute - function key mode sends x-functions-key header") {
            mockkConstructor(OkHttpClient::class)
            val project = ProjectBuilder.builder().build()
            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()
            justRun { mockResponse.close() }
            every { mockCall.execute() } returns mockResponse
            val requestSlot = slot<Request>()
            every { anyConstructed<OkHttpClient>().newCall(capture(requestSlot)) } returns mockCall

            val params = project.objects.newInstance<CallFunctionAction.Parameters>()
            params.uri.set("https://myapp.azurewebsites.net/api/myfunction")
            params.functionKey(CredentialReference.Literal("test-function-key"))

            val action = object : CallFunctionAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.header("x-functions-key") shouldBe "test-function-key"
            requestSlot.captured.header("Authorization") shouldBe null
        }

        test("execute - azure AD mode sends Authorization Bearer header") {
            mockkConstructor(OkHttpClient::class)
            val project = ProjectBuilder.builder().build()
            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()
            justRun { mockResponse.close() }
            every { mockCall.execute() } returns mockResponse
            val requestSlot = slot<Request>()
            every { anyConstructed<OkHttpClient>().newCall(capture(requestSlot)) } returns mockCall

            val params = project.objects.newInstance<CallFunctionAction.Parameters>()
            params.uri.set("https://myapp.azurewebsites.net/api/myfunction")
            params.azureAdToken(CredentialReference.Literal("test-bearer-token"))

            val action = object : CallFunctionAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.header("Authorization") shouldBe "Bearer test-bearer-token"
            requestSlot.captured.header("x-functions-key") shouldBe null
        }

        test("execute - rejects plain HTTP URIs") {
            val project = ProjectBuilder.builder().build()
            val params = project.objects.newInstance<CallFunctionAction.Parameters>()
            params.uri.set("http://myapp.azurewebsites.net/api/myfunction")
            params.anonymous()

            val action = object : CallFunctionAction() {
                override fun getParameters() = params
            }
            val result = runCatching { action.execute() }
            result.isFailure shouldBe true
        }
    }
}
