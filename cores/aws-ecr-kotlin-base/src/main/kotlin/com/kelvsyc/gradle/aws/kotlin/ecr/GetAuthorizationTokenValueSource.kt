package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * **Deprecated — configuration cache unsafe.** Gradle serializes the result of every
 * [ValueSource.obtain] call to the configuration cache in plaintext. An authorization token
 * returned here will be stored in `.gradle/configuration-cache/`. For task-execution use cases
 * (e.g. `docker login`), retrieve the token inside a [org.gradle.workers.WorkAction] instead.
 * If the token is genuinely needed at configuration time (e.g. for Maven repository authentication),
 * be aware of the cache-storage implication.
 *
 * **Task-field storage is also unsafe.** Gradle's config-cache codec walks the entire task
 * object graph — including `@get:Internal` properties and private `val` fields — and resolves
 * all `Provider` values at cache-write time. Wiring a `Provider` backed by this `ValueSource`
 * into any task field (annotated or not) causes `obtain()` to run at configuration time and the
 * token to be stored on disk. For task-execution use cases, use this `ValueSource` only entirely
 * within a `WorkAction.execute()` body; calling the build service client directly there is
 * simpler and avoids the `ValueSource` abstraction overhead.
 *
 * [ValueSource] implementation retrieving an authorization token from AWS ECR for the caller's default
 * registry.
 *
 * Returns the base64-encoded `user:password` token suitable for `docker login`. The result is the first entry
 * of [aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenResponse.authorizationData].
 */
@Deprecated(
    message = "ValueSource results are serialized to the Gradle configuration cache; authorization tokens " +
        "returned by obtain() are stored in plaintext in .gradle/configuration-cache/. " +
        "For task-execution use cases such as docker login, retrieve the token inside a " +
        "WorkAction instead. If the token is required at configuration time (e.g. for repository " +
        "authentication), be aware that the token value will be cached.",
    level = DeprecationLevel.WARNING
)
abstract class GetAuthorizationTokenValueSource : ValueSource<String, GetAuthorizationTokenValueSource.Parameters> {
    /**
     * Parameters for [GetAuthorizationTokenValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the ECR client. */
        @get:Internal
        val service: Property<EcrClientBuildService>
    }

    override fun obtain(): String? {
        val request = GetAuthorizationTokenRequest {}

        return runBlocking {
            parameters.service.get().getClient().getAuthorizationToken(request)
                .authorizationData
                ?.firstOrNull()
                ?.authorizationToken
        }
    }
}
