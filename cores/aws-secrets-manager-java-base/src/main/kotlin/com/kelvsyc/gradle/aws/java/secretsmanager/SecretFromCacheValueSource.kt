package com.kelvsyc.gradle.aws.java.secretsmanager

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * **Deprecated — configuration cache unsafe.** Gradle serializes the result of every
 * [ValueSource.obtain] call to the configuration cache in plaintext. A secret value returned
 * here will be stored in `.gradle/configuration-cache/` and is readable by any process with
 * access to the build directory. Retrieve secrets inside a
 * [org.gradle.workers.WorkAction] at task execution time instead, where the value is never
 * written to the cache.
 *
 * [ValueSource] implementation backed by retrieving a secret from an in-memory Secrets Manager cache.
 *
 * Only string secrets are supported.
 */
@Deprecated(
    message = "ValueSource results are serialized to the Gradle configuration cache; the secret value " +
        "returned by obtain() is stored in plaintext in .gradle/configuration-cache/. " +
        "Retrieve secrets inside a WorkAction at task execution time instead, " +
        "where the value is never written to the cache.",
    level = DeprecationLevel.WARNING
)
abstract class SecretFromCacheValueSource : ValueSource<String, SecretFromCacheValueSource.Parameters> {
    /**
     * Parameters for [SecretFromCacheValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the secret cache. */
        @get:Internal
        val service: Property<SecretCacheBuildService>

        /** The name or ARN of the secret to retrieve. */
        val secretName: Property<String>
    }

    override fun obtain(): String? = parameters.service.get().getClient().getSecretString(parameters.secretName.get())
}
