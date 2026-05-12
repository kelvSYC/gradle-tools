package com.kelvsyc.gradle.aws.java.secretsmanager

import com.amazonaws.secretsmanager.caching.SecretCache
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation backed by retrieving a secret from an in-memory Secrets Manager cache.
 *
 * Only string secrets are supported.
 */
abstract class SecretFromCacheValueSource : ValueSource<String, SecretFromCacheValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing Secrets Manager clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [SecretCacheClientInfo]. */
        val clientName: Property<String>

        /** The name or ARN of the secret to retrieve. */
        val secretName: Property<String>
    }

    private val client: Provider<SecretCache> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): String? {
        return client.get().getSecretString(parameters.secretName.get())
    }
}
