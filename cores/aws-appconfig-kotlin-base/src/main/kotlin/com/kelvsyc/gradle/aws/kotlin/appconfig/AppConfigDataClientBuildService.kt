package com.kelvsyc.gradle.aws.kotlin.appconfig

import aws.sdk.kotlin.services.appconfigdata.AppConfigDataClient
import aws.sdk.kotlin.services.appconfigdata.model.AppConfigDataException
import aws.sdk.kotlin.services.appconfigdata.model.GetLatestConfigurationRequest
import aws.sdk.kotlin.services.appconfigdata.model.StartConfigurationSessionRequest
import com.kelvsyc.gradle.aws.kotlin.AbstractAwsKotlinClientBuildService
import com.kelvsyc.gradle.aws.kotlin.AwsBuildServiceParams
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import org.gradle.api.logging.Logging
import java.util.concurrent.ConcurrentHashMap

/**
 * Build service managing an [AppConfigDataClient] instance with session-token caching.
 *
 * Wraps the AppConfig Data API's two-step session protocol. Call [fetchConfiguration] to retrieve
 * the current deployed configuration for a given application, environment, and configuration
 * profile. Session tokens are cached per (application, environment, profile) triple for the build
 * lifetime, so repeated calls within the same build reuse the existing session.
 */
abstract class AppConfigDataClientBuildService :
    AbstractAwsKotlinClientBuildService<AppConfigDataClient, AwsBuildServiceParams>() {

    companion object {
        private val logger = Logging.getLogger(AppConfigDataClientBuildService::class.java)
    }

    private data class SessionKey(
        val applicationIdentifier: String,
        val environmentIdentifier: String,
        val configurationProfileIdentifier: String,
    )

    private val sessionTokens = ConcurrentHashMap<SessionKey, String>()
    private val sessionMutex = Mutex()

    override fun createClient(): AppConfigDataClient = AppConfigDataClient {
        resolveRegion()?.let { region = it }
        resolveCredentialsProvider()?.let { credentialsProvider = it }
    }

    private suspend fun getOrCreateToken(key: SessionKey): String? {
        val cached = sessionTokens[key]
        if (cached != null) return cached
        return sessionMutex.withLock {
            sessionTokens[key] ?: run {
                val newToken = getClient().startConfigurationSession(
                    StartConfigurationSessionRequest {
                        applicationIdentifier = key.applicationIdentifier
                        environmentIdentifier = key.environmentIdentifier
                        configurationProfileIdentifier = key.configurationProfileIdentifier
                    }
                ).initialConfigurationToken
                if (newToken != null) sessionTokens[key] = newToken
                newToken
            }
        }
    }

    /**
     * Retrieves the current deployed configuration for the given identifiers.
     *
     * Manages the AppConfig Data session-token protocol internally. Starts a new session on the
     * first call for a given [applicationIdentifier]/[environmentIdentifier]/
     * [configurationProfileIdentifier] combination, then reuses the cached token on subsequent
     * calls within the same build. Session creation is serialized via a mutex to prevent duplicate
     * sessions under concurrent access.
     *
     * @return the configuration content as a [ByteArray], or `null` if the request fails.
     */
    open suspend fun fetchConfiguration(
        applicationIdentifier: String,
        environmentIdentifier: String,
        configurationProfileIdentifier: String,
    ): ByteArray? {
        return try {
            val key = SessionKey(applicationIdentifier, environmentIdentifier, configurationProfileIdentifier)
            val token = getOrCreateToken(key) ?: return null
            val response = getClient().getLatestConfiguration(
                GetLatestConfigurationRequest {
                    configurationToken = token
                }
            )
            sessionTokens[key] = response.nextPollConfigurationToken ?: token
            response.configuration
        } catch (e: AppConfigDataException) {
            logger.warn("Unable to fetch AppConfig configuration for $applicationIdentifier/$environmentIdentifier/$configurationProfileIdentifier", e)
            null
        }
    }
}
