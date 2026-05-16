package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Abstract base [WorkAction] that retrieves an ECR authorization token at task execution time and
 * passes it to [doExecute], keeping the token out of the Gradle configuration cache.
 *
 * The token is obtained inside [execute], which is marked `final` to ensure it always runs at task
 * execution time. The [runBlocking] call wraps the Kotlin SDK's suspend function.
 *
 * ## Token lifecycle
 *
 * ECR authorization tokens are valid for up to 12 hours. There is **no explicit revocation API** —
 * the token self-expires at the end of its validity period, regardless of whether [doExecute]
 * returns normally or throws. This is unlike Vault dynamic credentials, which carry a lease that
 * can be explicitly revoked in a `finally` block. [doExecute] therefore provides the correct and
 * complete execution-time scope for this token.
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
 * abstract class DockerLoginAction : AbstractGetAuthorizationTokenWorkAction() {
 *     override fun doExecute(token: String) {
 *         // token is the base64-encoded "AWS:password" string suitable for docker login
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
         * The build service managing the ECR client.
         * Excluded from task snapshots.
         */
        @get:Internal
        val service: Property<EcrClientBuildService>
    }

    final override fun execute() {
        val token = runBlocking {
            parameters.service.get().getClient()
                .getAuthorizationToken(GetAuthorizationTokenRequest {})
                .authorizationData
                ?.first()
                ?.authorizationToken
        } ?: error("No authorization data returned from ECR")
        doExecute(token)
    }

    /**
     * Executes work using the retrieved authorization [token].
     *
     * Called at task execution time with a valid ECR token. The token self-expires after up to
     * 12 hours — there is no explicit revocation API.
     *
     * @param token The base64-encoded `AWS:password` authorization token suitable for `docker login`.
     */
    protected abstract fun doExecute(token: String)
}
