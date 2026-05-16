package com.kelvsyc.gradle.azure.identity

import com.azure.core.credential.TokenRequestContext
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Abstract base [WorkAction] that retrieves an OAuth2 access token from Azure Managed Identity
 * and executes work with it, keeping the token out of the Gradle configuration cache.
 *
 * ## Token lifecycle
 *
 * Azure AD access tokens are typically valid for 1 hour and have **no explicit revocation API**.
 * Unlike Vault's dynamic credentials (which support immediate lease revocation), Azure AD tokens
 * expire passively only. The token returned by this action should be used immediately within
 * [doExecute] and not cached or persisted.
 *
 * ## Configuration cache safety
 *
 * Do not let the token escape [doExecute]. The ways it can leak:
 *
 * - **WorkParameters property** (even `@get:Internal`): Gradle serializes all WorkParameters to
 *   `.gradle/configuration-cache/` in plaintext.
 * - **Task input, output, or property**: same serialization path.
 * - **Shared file or static field**: the value persists on disk beyond this build invocation.
 *
 * Subclass this action and implement [doExecute] to use the token:
 *
 * ```kotlin
 * abstract class CallAzureApiAction : AbstractGetAccessTokenWorkAction() {
 *     override fun doExecute(token: String) {
 *         // token is the raw OAuth2 bearer token for the requested scopes
 *     }
 * }
 * ```
 *
 * ## execute() is final
 *
 * The [execute] method is final to enforce the contract: retrieve the token via the parameters,
 * pass it to [doExecute], and nothing else. Subclasses must implement [doExecute] to define
 * work that uses the token.
 */
abstract class AbstractGetAccessTokenWorkAction :
    WorkAction<AbstractGetAccessTokenWorkAction.Parameters> {

    /**
     * Parameters for [AbstractGetAccessTokenWorkAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The managed identity build service used to retrieve the access token.
         * Excluded from task snapshots.
         */
        @get:Internal
        val service: Property<ManagedIdentityCredentialBuildService>

        /**
         * OAuth2 scopes to request for the access token.
         *
         * At least one scope is required. Common values:
         * - `https://management.azure.com/.default` — Azure Resource Manager
         * - `https://graph.microsoft.com/.default` — Microsoft Graph
         * - `https://vault.azure.net/.default` — Azure Key Vault
         */
        val scopes: ListProperty<String>
    }

    final override fun execute() {
        val context = TokenRequestContext()
        parameters.scopes.get().forEach { context.addScopes(it) }
        val token = parameters.service.get().getClient().getToken(context).block()?.token
            ?: error("No access token returned from managed identity endpoint")
        doExecute(token)
    }

    /**
     * Executes work using the retrieved access [token].
     *
     * Called immediately after the token is retrieved. The token is valid for approximately
     * 1 hour and will be used within this method's execution context. The token cannot be
     * explicitly revoked — it expires passively after its expiry time.
     *
     * @param token The raw OAuth2 bearer token string valid for the requested scopes.
     *   This is not cached and is execution-time-only.
     */
    protected abstract fun doExecute(token: String)
}
