package com.kelvsyc.gradle.aws.java.secretsmanager

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation backed by retrieving a secret from an in-memory Secrets Manager cache.
 *
 * Only string secrets are supported.
 */
abstract class SecretFromCacheValueSource : ValueSource<String, SecretFromCacheValueSource.Parameters> {
    /**
     * Parameters for [SecretFromCacheValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the secret cache. */
        val service: Property<SecretCacheBuildService>

        /** The name or ARN of the secret to retrieve. */
        val secretName: Property<String>
    }

    override fun obtain(): String? = parameters.service.get().getClient().getSecretString(parameters.secretName.get())
}
