package com.kelvsyc.gradle.aws.java.ecr

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenRequest

/**
 * **Deprecated — configuration cache unsafe.** Gradle serializes the result of every
 * [ValueSource.obtain] call to the configuration cache in plaintext. An authorization token
 * returned here will be stored in `.gradle/configuration-cache/`. For task-execution use cases
 * (e.g. `docker login`), retrieve the token inside a [org.gradle.workers.WorkAction] instead.
 * If the token is genuinely needed at configuration time (e.g. for Maven repository authentication),
 * be aware of the cache-storage implication.
 *
 * [ValueSource] implementation retrieving an authorization token from AWS ECR for the caller's default
 * registry.
 *
 * Returns the base64-encoded `user:password` token suitable for `docker login`. The result is the first entry
 * of [software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenResponse.authorizationData].
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
    interface Parameters : ValueSourceParameters {
        /** The build service managing the ECR client. */
        @get:Internal
        val service: Property<EcrClientBuildService>
    }

    override fun obtain(): String? {
        val request = GetAuthorizationTokenRequest.builder().build()

        return parameters.service.get().getClient()
            .getAuthorizationToken(request)
            .authorizationData()
            .firstOrNull()
            ?.authorizationToken()
    }
}
