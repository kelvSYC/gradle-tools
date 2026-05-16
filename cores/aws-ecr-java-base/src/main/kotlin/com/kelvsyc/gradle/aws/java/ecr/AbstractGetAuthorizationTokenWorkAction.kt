package com.kelvsyc.gradle.aws.java.ecr

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenRequest

/**
 * Abstract base [WorkAction] that retrieves an ECR authorization token and executes work with it,
 * keeping the token out of the Gradle configuration cache.
 *
 * ## Token lifecycle
 *
 * ECR authorization tokens are valid for up to 12 hours and have no explicit revocation API.
 * Unlike Vault's dynamic credentials (which support immediate lease revocation), ECR tokens
 * expire passively. The token returned by this action should be used immediately within
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
 * abstract class DockerLoginAction : AbstractGetAuthorizationTokenWorkAction() {
 *     override fun doExecute(token: String) {
 *         // token is the base64-encoded "AWS:password" string suitable for docker login
 *     }
 * }
 * ```
 */
abstract class AbstractGetAuthorizationTokenWorkAction :
    WorkAction<AbstractGetAuthorizationTokenWorkAction.Parameters> {

    /** Parameters for [AbstractGetAuthorizationTokenWorkAction]. */
    interface Parameters : WorkParameters {
        /**
         * The ECR build service used to retrieve the authorization token.
         * Excluded from task snapshots.
         */
        @get:Internal
        val service: Property<EcrClientBuildService>
    }

    final override fun execute() {
        val token = parameters.service.get().getClient()
            .getAuthorizationToken(GetAuthorizationTokenRequest.builder().build())
            .authorizationData().first().authorizationToken()
        doExecute(token)
    }

    /**
     * Executes work using the retrieved [token].
     *
     * Called immediately after the token is retrieved. The token is valid for up to 12 hours
     * and will be used within this method's execution context.
     *
     * @param token The base64-encoded authorization token in the format "AWS:password", suitable
     *   for use with `docker login` or repository authentication. This is not cached and is
     *   execution-time-only.
     */
    protected abstract fun doExecute(token: String)
}
