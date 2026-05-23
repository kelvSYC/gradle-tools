package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfigdata.AppConfigDataClient
import aws.sdk.kotlin.services.appconfigdata.model.AppConfigDataException
import aws.sdk.kotlin.services.appconfigdata.model.GetLatestConfigurationResponse
import aws.sdk.kotlin.services.appconfigdata.model.StartConfigurationSessionResponse
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.shouldNotBe
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.runBlocking
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

internal abstract class StubAppConfigDataClientBuildService : AppConfigDataClientBuildService() {
    override fun createClient(): AppConfigDataClient = checkNotNull(stubClient) { "stubClient not set" }
    companion object { var stubClient: AppConfigDataClient? = null }
}

class AppConfigDataClientBuildServiceSpec : FunSpec({
    fun buildService(name: String) = ProjectBuilder.builder().build().gradle.sharedServices
        .registerIfAbsent(name, StubAppConfigDataClientBuildService::class)

    test("fetchConfiguration starts session and returns configuration bytes") {
        val client = mockk<AppConfigDataClient>()
        StubAppConfigDataClientBuildService.stubClient = client
        coEvery { client.startConfigurationSession(any()) } returns StartConfigurationSessionResponse {
            initialConfigurationToken = "token-1"
        }
        coEvery { client.getLatestConfiguration(any()) } returns GetLatestConfigurationResponse {
            configuration = "config-data".toByteArray()
            nextPollConfigurationToken = "token-2"
        }

        val result = runBlocking {
            buildService("svc-1").get().fetchConfiguration("app", "env", "profile")
        }

        result shouldNotBe null
        result!!.toString(Charsets.UTF_8) shouldBe "config-data"
        StubAppConfigDataClientBuildService.stubClient = null
    }

    test("fetchConfiguration returns null when initialConfigurationToken is absent") {
        val client = mockk<AppConfigDataClient>()
        StubAppConfigDataClientBuildService.stubClient = client
        coEvery { client.startConfigurationSession(any()) } returns StartConfigurationSessionResponse {
            initialConfigurationToken = null
        }

        val result = runBlocking {
            buildService("svc-2").get().fetchConfiguration("app", "env", "profile")
        }

        result shouldBe null
        StubAppConfigDataClientBuildService.stubClient = null
    }

    test("fetchConfiguration caches the session token across calls") {
        val client = mockk<AppConfigDataClient>()
        StubAppConfigDataClientBuildService.stubClient = client
        coEvery { client.startConfigurationSession(any()) } returns StartConfigurationSessionResponse {
            initialConfigurationToken = "token-1"
        }
        coEvery { client.getLatestConfiguration(any()) } returns GetLatestConfigurationResponse {
            configuration = "data".toByteArray()
            nextPollConfigurationToken = "token-2"
        }

        val service = buildService("svc-3").get()
        runBlocking {
            service.fetchConfiguration("app", "env", "profile")
            service.fetchConfiguration("app", "env", "profile")
        }

        coVerify(exactly = 1) { client.startConfigurationSession(any()) }
        coVerify(exactly = 2) { client.getLatestConfiguration(any()) }
        StubAppConfigDataClientBuildService.stubClient = null
    }

    test("fetchConfiguration returns null on AppConfigDataException") {
        val client = mockk<AppConfigDataClient>()
        StubAppConfigDataClientBuildService.stubClient = client
        coEvery { client.startConfigurationSession(any()) } throws mockk<AppConfigDataException>()

        val result = runBlocking {
            buildService("svc-4").get().fetchConfiguration("app", "env", "profile")
        }

        result shouldBe null
        StubAppConfigDataClientBuildService.stubClient = null
    }
})
