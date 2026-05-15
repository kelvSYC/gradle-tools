package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import software.amazon.awssdk.services.codeartifact.model.CodeartifactException
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest

/**
 * **Deprecated — configuration cache unsafe.** Gradle serializes the result of every
 * [ValueSource.obtain] call to the configuration cache in plaintext. An authorization token
 * returned here will be stored in `.gradle/configuration-cache/`. For task-execution use cases
 * (e.g. `docker login`), retrieve the token inside a [org.gradle.workers.WorkAction] instead.
 * If the token is genuinely needed at configuration time (e.g. for Maven repository authentication),
 * be aware of the cache-storage implication.
 *
 * [ValueSource] implementation retrieving an authorization token from AWS CodeArtifact.
 *
 * The value is obtained from a request to AWS CodeArtifact.
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
        /** The build service managing the CodeArtifact client. */
        @get:Internal
        val service: Property<CodeArtifactClientBuildService>

        /** The CodeArtifact domain name. */
        val domain: Property<String>

        /** The 12-digit account number of the domain owner. */
        val domainOwner: Property<String>

        /**
         * The time, in seconds, that the authorization token is valid.
         *
         * @see [GetAuthorizationTokenRequest.durationSeconds]
         */
        val duration: Property<Long>
    }

    override fun obtain(): String? {
        val request = GetAuthorizationTokenRequest.builder().apply {
            domain(parameters.domain.get())
            domainOwner(parameters.domainOwner.get())

            durationSeconds(parameters.duration.get())
        }.build()

        return try {
            val response = parameters.service.get().getClient().getAuthorizationToken(request)
            response.authorizationToken()
        } catch (_: CodeartifactException) {
            null
        }
    }
}
