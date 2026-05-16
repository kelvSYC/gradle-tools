package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.model.GetAuthorizationTokenRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Abstract base [WorkAction] that retrieves a CodeArtifact authorization token and executes work
 * with it, keeping the token out of the Gradle configuration cache.
 *
 * ## Token lifecycle
 *
 * CodeArtifact authorization tokens are valid for up to 12 hours and have no explicit revocation
 * API. Unlike Vault's dynamic credentials (which support immediate lease revocation), CodeArtifact
 * tokens expire passively. The token returned by this action should be used immediately within
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
 * abstract class PublishArtifactAction : AbstractGetAuthorizationTokenWorkAction() {
 *     override fun doExecute(token: String) {
 *         // token is the authorization token suitable for CodeArtifact repository authentication
 *     }
 * }
 * ```
 */
abstract class AbstractGetAuthorizationTokenWorkAction :
    WorkAction<AbstractGetAuthorizationTokenWorkAction.Parameters> {

    /**
     * Parameters for [AbstractGetAuthorizationTokenWorkAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The CodeArtifact build service used to retrieve the authorization token.
         * Excluded from task snapshots.
         */
        @get:Internal
        val service: Property<CodeArtifactClientBuildService>

        /**
         * The CodeArtifact domain name.
         */
        val domain: Property<String>

        /**
         * The 12-digit account number of the domain owner.
         */
        val domainOwner: Property<String>

        /**
         * The time, in seconds, that the authorization token is valid.
         *
         * Valid range is 900 to 43200 seconds (15 minutes to 12 hours).
         */
        val duration: Property<Long>
    }

    final override fun execute() {
        val request = GetAuthorizationTokenRequest {
            domain = parameters.domain.get()
            domainOwner = parameters.domainOwner.get()
            durationSeconds = parameters.duration.get()
        }
        val token = runBlocking {
            parameters.service.get().getClient().getAuthorizationToken(request).authorizationToken
        } ?: error("No authorization token returned from CodeArtifact")
        doExecute(token)
    }

    /**
     * Executes work using the retrieved [token].
     *
     * Called immediately after the token is retrieved. The token is valid for up to 12 hours
     * and will be used within this method's execution context.
     *
     * @param token The authorization token suitable for use with CodeArtifact repository
     *   authentication. This is not cached and is execution-time-only.
     */
    protected abstract fun doExecute(token: String)
}
