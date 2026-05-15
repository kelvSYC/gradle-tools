package com.kelvsyc.gradle.azure.identity

import com.azure.core.credential.TokenRequestContext
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] that obtains an OAuth2 access token for the specified scopes using a
 * [ManagedIdentityCredentialBuildService].
 *
 * The returned string is the raw bearer token value. Obtain a scoped token for Azure Resource
 * Manager via scope `https://management.azure.com/.default`.
 */
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
