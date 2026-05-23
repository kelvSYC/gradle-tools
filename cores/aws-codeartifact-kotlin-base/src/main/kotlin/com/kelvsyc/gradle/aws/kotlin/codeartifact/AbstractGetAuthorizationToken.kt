package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.model.GetAuthorizationTokenRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Abstract base task that retrieves a CodeArtifact authorization token and executes work
 * with it, keeping the token out of the Gradle configuration cache.
 *
 * ## Token lifecycle
 *
 * CodeArtifact authorization tokens are valid for up to 12 hours and have no explicit revocation
 * API. Unlike Vault's dynamic credentials (which support immediate lease revocation), CodeArtifact
 * tokens expire passively. The token returned by this task should be used immediately within
 * [doExecute] and not cached or persisted.
 *
 * ## Configuration cache safety
 *
 * Do not let the token escape [doExecute]. The ways it can leak:
 *
 * - **Task property** (even `@get:Internal`): Gradle serializes all task properties to
 *   `.gradle/configuration-cache/` in plaintext.
 * - **Task input or output**: same serialization path.
 * - **Shared file or static field**: the value persists on disk beyond this build invocation.
 *
 * Subclass this task and implement [doExecute] to use the token:
 *
 * ```kotlin
 * abstract class PublishArtifact : AbstractGetAuthorizationToken() {
 *     override fun doExecute(token: String) {
 *         // token is the authorization token suitable for CodeArtifact repository authentication
 *     }
 * }
 * ```
 */
@UntrackedTask(because = "Communicates with AWS CodeArtifact; no local output")
abstract class AbstractGetAuthorizationToken : DefaultTask() {

    /**
     * The CodeArtifact build service used to retrieve the authorization token.
     * Excluded from task snapshots.
     */
    @get:Internal
    abstract val service: Property<CodeArtifactClientBuildService>

    /**
     * The CodeArtifact domain name.
     */
    @get:Input
    abstract val domain: Property<String>

    /**
     * The 12-digit account number of the domain owner.
     */
    @get:Input
    abstract val domainOwner: Property<String>

    /**
     * The time, in seconds, that the authorization token is valid.
     *
     * Valid range is 900 to 43200 seconds (15 minutes to 12 hours).
     */
    @get:Input
    abstract val duration: Property<Long>

    /**
     * Executes this task by fetching the authorization token and calling [doExecute].
     *
     * The token is retrieved at task execution time and passed to [doExecute], which executes
     * immediately. The token is never stored in the configuration cache.
     */
    @TaskAction
    fun execute() {
        val request = GetAuthorizationTokenRequest {
            domain = this@AbstractGetAuthorizationToken.domain.get()
            domainOwner = this@AbstractGetAuthorizationToken.domainOwner.get()
            durationSeconds = this@AbstractGetAuthorizationToken.duration.get()
        }
        val token = runBlocking {
            service.get().getClient().getAuthorizationToken(request).authorizationToken
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
