package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfigdata.AppConfigDataClient

/**
 * Test-only [AppConfigDataClientBuildService] that overrides [fetchConfiguration] directly,
 * decoupling tests from the AppConfig Data session protocol.
 */
abstract class MockAppConfigDataClientBuildService : AppConfigDataClientBuildService() {
    override fun createClient(): AppConfigDataClient = error("Not used in tests")

    override suspend fun fetchConfiguration(
        applicationIdentifier: String,
        environmentIdentifier: String,
        configurationProfileIdentifier: String,
    ): ByteArray? = fetchImpl?.invoke(applicationIdentifier, environmentIdentifier, configurationProfileIdentifier)

    companion object {
        var fetchImpl: (suspend (String, String, String) -> ByteArray?)? = null
    }
}
