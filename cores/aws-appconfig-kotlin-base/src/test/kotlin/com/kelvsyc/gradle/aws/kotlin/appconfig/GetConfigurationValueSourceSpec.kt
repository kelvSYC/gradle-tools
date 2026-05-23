package com.kelvsyc.gradle.aws.kotlin.appconfig

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.nulls.shouldBeNull
import io.kotest.matchers.shouldBe
import org.gradle.kotlin.dsl.registerIfAbsent
import org.gradle.testfixtures.ProjectBuilder

class GetConfigurationValueSourceSpec : FunSpec({
    test("obtain returns configuration as UTF-8 string on success") {
        val project = ProjectBuilder.builder().build()
        MockAppConfigDataClientBuildService.fetchImpl = { _, _, _ ->
            """{"enabled":true}""".toByteArray()
        }
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig-data", MockAppConfigDataClientBuildService::class)

        val provider = project.providers.ofKt(GetConfigurationValueSource::class) {
            parameters.service.set(service)
            parameters.applicationIdentifier.set("my-app")
            parameters.environmentIdentifier.set("production")
            parameters.configurationProfileIdentifier.set("my-profile")
        }

        provider.get() shouldBe """{"enabled":true}"""
        MockAppConfigDataClientBuildService.fetchImpl = null
    }

    test("obtain returns null when fetchConfiguration returns null") {
        val project = ProjectBuilder.builder().build()
        MockAppConfigDataClientBuildService.fetchImpl = { _, _, _ -> null }
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig-data", MockAppConfigDataClientBuildService::class)

        val provider = project.providers.ofKt(GetConfigurationValueSource::class) {
            parameters.service.set(service)
            parameters.applicationIdentifier.set("missing-app")
            parameters.environmentIdentifier.set("production")
            parameters.configurationProfileIdentifier.set("missing-profile")
        }

        provider.orNull.shouldBeNull()
        MockAppConfigDataClientBuildService.fetchImpl = null
    }

    test("obtain returns null when fetchConfiguration returns empty bytes") {
        val project = ProjectBuilder.builder().build()
        MockAppConfigDataClientBuildService.fetchImpl = { _, _, _ -> ByteArray(0) }
        val service = project.gradle.sharedServices
            .registerIfAbsent("appconfig-data", MockAppConfigDataClientBuildService::class)

        val provider = project.providers.ofKt(GetConfigurationValueSource::class) {
            parameters.service.set(service)
            parameters.applicationIdentifier.set("my-app")
            parameters.environmentIdentifier.set("production")
            parameters.configurationProfileIdentifier.set("my-profile")
        }

        provider.orNull.shouldBeNull()
        MockAppConfigDataClientBuildService.fetchImpl = null
    }
})
