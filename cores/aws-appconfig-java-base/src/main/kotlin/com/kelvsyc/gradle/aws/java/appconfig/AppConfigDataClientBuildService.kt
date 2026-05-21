package com.kelvsyc.gradle.aws.java.appconfig

import com.kelvsyc.gradle.aws.java.AbstractAwsJavaClientBuildService
import com.kelvsyc.gradle.aws.java.AwsBuildServiceParams
import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import software.amazon.awssdk.services.appconfigdata.AppConfigDataClient
import software.amazon.awssdk.services.appconfigdata.model.AppConfigDataException
import software.amazon.awssdk.services.appconfigdata.model.GetLatestConfigurationRequest
import software.amazon.awssdk.services.appconfigdata.model.StartConfigurationSessionRequest
import java.util.concurrent.ConcurrentHashMap

/**
 * Build service managing an [AppConfigDataClient] instance with session-token caching.
 *
 * Wraps the AppConfig Data API's two-step session protocol. Call [fetchConfiguration] to retrieve
 * the current deployed configuration for a given application, environment, and configuration profile.
 * Session tokens are cached per (application, environment, profile) triple for the build lifetime,
 * so repeated calls within the same build reuse the existing session.
 */
abstract class AppConfigDataClientBuildService :
    AbstractAwsJavaClientBuildService<AppConfigDataClient, AwsBuildServiceParams>() {

    companion object {
        val logger by GradleLoggerDelegate
    }

    private data class SessionKey(
        val applicationIdentifier: String,
        val environmentIdentifier: String,
        val configurationProfileIdentifier: String,
    )

    private val sessionTokens = ConcurrentHashMap<SessionKey, String>()

    override fun createClient(): AppConfigDataClient =
        configureBuilder(AppConfigDataClient.builder()).build()

    /**
     * Retrieves the current deployed configuration for the given identifiers.
     *
     * Manages the AppConfig Data session-token protocol internally. Starts a new session on the first
     * call for a given [applicationIdentifier]/[environmentIdentifier]/[configurationProfileIdentifier]
     * combination, then reuses the cached token on subsequent calls within the same build.
     *
     * @return the configuration content as a [ByteArray], or `null` if the request fails.
     */
    fun fetchConfiguration(
        applicationIdentifier: String,
        environmentIdentifier: String,
        configurationProfileIdentifier: String,
    ): ByteArray? {
        return try {
            val key = SessionKey(applicationIdentifier, environmentIdentifier, configurationProfileIdentifier)
            val token = sessionTokens.getOrPut(key) {
                val sessionRequest = StartConfigurationSessionRequest.builder()
                    .applicationIdentifier(applicationIdentifier)
                    .environmentIdentifier(environmentIdentifier)
                    .configurationProfileIdentifier(configurationProfileIdentifier)
                    .build()
                getClient().startConfigurationSession(sessionRequest).initialConfigurationToken()
            }
            val configRequest = GetLatestConfigurationRequest.builder()
                .configurationToken(token)
                .build()
            val response = getClient().getLatestConfiguration(configRequest)
            sessionTokens[key] = response.nextPollConfigurationToken()
            response.configuration().asByteArray()
        } catch (e: AppConfigDataException) {
            logger.warn(e) { "Unable to fetch AppConfig configuration for $applicationIdentifier/$environmentIdentifier/$configurationProfileIdentifier" }
            null
        }
    }
}
