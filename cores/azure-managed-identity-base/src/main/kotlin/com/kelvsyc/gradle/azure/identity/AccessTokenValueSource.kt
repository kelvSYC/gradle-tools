package com.kelvsyc.gradle.azure.identity

import com.azure.core.credential.TokenRequestContext
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * **Deprecated — configuration cache unsafe.** Gradle serializes the result of every
 * [ValueSource.obtain] call to the configuration cache in plaintext. An OAuth2 token
 * returned here will be stored in `.gradle/configuration-cache/`. For task-execution use cases,
 * retrieve the token inside a [org.gradle.workers.WorkAction] instead.
 * If the token is genuinely needed at configuration time, be aware of the cache-storage implication.
 *
 * [ValueSource] that obtains an OAuth2 access token for the specified scopes using a
 * [ManagedIdentityCredentialBuildService].
 *
 * The returned string is the raw bearer token value. Obtain a scoped token for Azure Resource
 * Manager via scope `https://management.azure.com/.default`.
 */
@Deprecated(
    message = "ValueSource results are serialized to the Gradle configuration cache; OAuth2 tokens " +
        "returned by obtain() are stored in plaintext in .gradle/configuration-cache/. " +
        "For task-execution use cases, retrieve the token inside a WorkAction instead. " +
        "If the token is required at configuration time, be aware that the value will be cached.",
    level = DeprecationLevel.WARNING
)
abstract class AccessTokenValueSource : ValueSource<String, AccessTokenValueSource.Parameters> {

    /**
     * Parameters for [AccessTokenValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The build service managing the managed identity credential.
         */
        @get:Internal
        val service: Property<ManagedIdentityCredentialBuildService>

        /**
         * OAuth2 scopes to request. At least one scope must be provided.
         */
        val scopes: ListProperty<String>
    }

    override fun obtain(): String? {
        val context = TokenRequestContext()
        parameters.scopes.get().forEach { context.addScopes(it) }
        return parameters.service.get().getClient().getToken(context).block()?.token
    }
}
