package com.kelvsyc.gradle.aws.java.appconfig

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient
import software.amazon.awssdk.services.appconfigdata.model.AppConfigDataException
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationResponse
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionResponse

class GetConfigurationValueSourceSpec : FunSpec() {
    init {
        test("obtain - returns configuration as UTF-8 string on success") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigDataClient>()
            MockAppConfigDataClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig-data", MockAppConfigDataClientBuildService::class)

            val sessionResponse = mockk<StartConfigurationSessionResponse>()
            every { sessionResponse.initialConfigurationToken() } returns "initial-token"
            every { client.startConfigurationSession(any<StartConfigurationSessionRequest>()) } returns sessionResponse

            val configResponse = mockk<GetLatestConfigurationResponse>()
            every { configResponse.configuration() } returns SdkBytes.fromUtf8String("""{"enabled":true}""")
            every { configResponse.nextPollConfigurationToken() } returns "next-token"
            every { client.getLatestConfiguration(any<GetLatestConfigurationRequest>()) } returns configResponse

            val provider = project.providers.ofKt(GetConfigurationValueSource::class) {
                parameters.service.set(service)
                parameters.applicationIdentifier.set("my-app")
                parameters.environmentIdentifier.set("production")
                parameters.configurationProfileIdentifier.set("my-profile")
            }

            provider.get() shouldBe """{"enabled":true}"""
        }

        test("obtain - returns null when AppConfigDataException is thrown") {
            val project = ProjectBuilder.builder().build()
            val client = mockk<AppConfigDataClient>()
            MockAppConfigDataClientBuildService.mockClient = client
            val service = project.gradle.sharedServices
                .registerIfAbsent("appconfig-data", MockAppConfigDataClientBuildService::class)

            every {
                client.startConfigurationSession(any<StartConfigurationSessionRequest>())
            } throws AppConfigDataException.builder().message("not found").build()

            val provider = project.providers.ofKt(GetConfigurationValueSource::class) {
                parameters.service.set(service)
                parameters.applicationIdentifier.set("missing-app")
                parameters.environmentIdentifier.set("production")
                parameters.configurationProfileIdentifier.set("missing-profile")
            }

            provider.orNull.shouldBeNull()
        }
    }
}
