package com.kelvsyc.gradle.aws.java.secretsmanager

import com.amazonaws.secretsmanager.caching.SecretCache
import com.amazonaws.secretsmanager.caching.SecretCacheConfiguration
import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters

/**
 * Build service managing a [SecretCache] in front of a [SecretsManagerClientBuildService].
 *
 * Register an instance via [org.gradle.api.services.BuildServiceRegistry.registerIfAbsent], setting
 * [Params.baseService] to a registered [SecretsManagerClientBuildService]. The wrapped client is resolved
 * lazily — the underlying service is not instantiated until this service's [createClient] runs.
 */
abstract class SecretCacheBuildService :
    AbstractClientBuildService<SecretCache, SecretCacheBuildService.Params>() {
    /**
     * Configuration parameters for [SecretCacheBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The build service supplying the underlying [SecretsManagerClient][software.amazon.awssdk.services.secretsmanager.SecretsManagerClient].
         */
        val baseService: Property<SecretsManagerClientBuildService>

        /** Maximum number of cached secrets. */
        val maxCacheSize: Property<Int>

        /** Cache TTL in milliseconds. */
        val cacheItemTtl: Property<Long>
    }

    override fun createClient(): SecretCache {
        val configuration = SecretCacheConfiguration().apply {
            client = parameters.baseService.get().getClient()
            if (parameters.maxCacheSize.isPresent) {
                maxCacheSize = parameters.maxCacheSize.get()
            }
            if (parameters.cacheItemTtl.isPresent) {
                cacheItemTTL = parameters.cacheItemTtl.get()
            }
        }
        return SecretCache(configuration)
    }
}
