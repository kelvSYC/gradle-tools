package com.kelvsyc.gradle.aws.java.secretsmanager

import com.amazonaws.secretsmanager.caching.SecretCache
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation backed by retrieving a secret from an in-memory Secrets Manager cache.
 *
 * Only string secrets are supported.
 */
abstract class SecretFromCacheValueSource : ValueSource<String, SecretFromCacheValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val client: Property<SecretCache>

        val secretName: Property<String>
    }

    override fun obtain(): String? {
        return parameters.client.get().getSecretString(parameters.secretName.get())
    }
}
