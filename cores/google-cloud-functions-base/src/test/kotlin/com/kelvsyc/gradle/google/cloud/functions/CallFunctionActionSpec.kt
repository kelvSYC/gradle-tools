package com.kelvsyc.gradle.google.cloud.functions

import com.kelvsyc.gradle.clients.CredentialReference
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
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

        test("execute - sends POST to function URI without auth header when token absent") {
            mockkConstructor(OkHttpClient::class)
            val project = ProjectBuilder.builder().build()

            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()
            justRun { mockResponse.close() }
            every { mockCall.execute() } returns mockResponse

            val requestSlot = slot<Request>()
            every { anyConstructed<OkHttpClient>().newCall(capture(requestSlot)) } returns mockCall

            val params = project.objects.newInstance<CallFunctionAction.Parameters>()
            params.uri.set("https://us-central1-my-project.cloudfunctions.net/my-function")

            val action = object : CallFunctionAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.url.toString() shouldBe
                "https://us-central1-my-project.cloudfunctions.net/my-function"
            requestSlot.captured.method shouldBe "POST"
            requestSlot.captured.header("Authorization") shouldBe null
        }

        test("execute - sends Authorization header when identity token is present") {
            mockkConstructor(OkHttpClient::class)
            val project = ProjectBuilder.builder().build()

            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()
            justRun { mockResponse.close() }
            every { mockCall.execute() } returns mockResponse

            val requestSlot = slot<Request>()
            every { anyConstructed<OkHttpClient>().newCall(capture(requestSlot)) } returns mockCall

            val params = project.objects.newInstance<CallFunctionAction.Parameters>()
            params.uri.set("https://us-central1-my-project.cloudfunctions.net/my-function")
            params.identityToken.set(CredentialReference.Literal("test-token-123"))

            val action = object : CallFunctionAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.header("Authorization") shouldBe "Bearer test-token-123"
        }

        test("execute - includes payload body when payload is set") {
            mockkConstructor(OkHttpClient::class)
            val project = ProjectBuilder.builder().build()

            val mockCall = mockk<Call>()
            val mockResponse = mockk<Response>()
            justRun { mockResponse.close() }
            every { mockCall.execute() } returns mockResponse

            val requestSlot = slot<Request>()
            every { anyConstructed<OkHttpClient>().newCall(capture(requestSlot)) } returns mockCall

            val params = project.objects.newInstance<CallFunctionAction.Parameters>()
            params.uri.set("https://us-central1-my-project.cloudfunctions.net/my-function")
            params.payload.set("""{"key":"value"}""")

            val action = object : CallFunctionAction() {
                override fun getParameters() = params
            }
            action.execute()

            requestSlot.captured.body shouldNotBe null
        }
    }
}
