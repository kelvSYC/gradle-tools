package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest

/**
 * Abstract base [WorkAction] that retrieves a CodeArtifact authorization token at task execution
 * time and passes it to [doExecute], keeping the token out of the Gradle configuration cache.
 *
 * The token is obtained inside [execute], which is marked `final` to ensure it always runs at task
 * execution time.
 *
 * ## Token lifecycle
 *
 * CodeArtifact authorization tokens are valid for the duration specified by [Parameters.duration]
 * (up to 43200 seconds / 12 hours). There is **no explicit revocation API** — the token
 * self-expires at the end of the configured duration, regardless of whether [doExecute] returns
 * normally or throws. This is unlike Vault dynamic credentials, which carry a lease that can be
 * explicitly revoked in a `finally` block. [doExecute] therefore provides the correct and complete
 * execution-time scope for this token.
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
 * Subclass and implement [doExecute] to use the token:
 *
 * ```kotlin
 * abstract class PublishAction : AbstractGetAuthorizationTokenWorkAction() {
 *     override fun doExecute(token: String) {
 *         // use token for Maven/npm/pip repository authentication
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
         * The build service managing the CodeArtifact client.
         * Excluded from task snapshots.
         */
        @get:Internal
        val service: Property<CodeArtifactClientBuildService>

        /** The CodeArtifact domain name. */
        val domain: Property<String>

        /** The 12-digit AWS account ID of the domain owner. */
        val domainOwner: Property<String>

        /**
         * The token validity duration in seconds. Maximum is 43200 (12 hours).
         *
         * @see GetAuthorizationTokenRequest.durationSeconds
         */
        val duration: Property<Long>
    }

    final override fun execute() {
        val request = GetAuthorizationTokenRequest.builder()
            .domain(parameters.domain.get())
            .domainOwner(parameters.domainOwner.get())
            .durationSeconds(parameters.duration.get())
            .build()
        val token = parameters.service.get().getClient().getAuthorizationToken(request).authorizationToken()
        doExecute(token)
    }

    /**
     * Executes work using the retrieved authorization [token].
     *
     * Called at task execution time with a valid CodeArtifact token. The token self-expires after
     * the configured [Parameters.duration] — there is no explicit revocation API.
     *
     * @param token The CodeArtifact authorization token.
     */
    protected abstract fun doExecute(token: String)
}
