package com.kelvsyc.gradle.aws.java.codeartifact

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.CodeartifactException
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest

/**
 * [ValueSource] implementation retrieving an authorization token from AWS CodeArtifact.
 *
 * The value is obtained from a request to AWS CodeArtifact.
 */
abstract class GetAuthorizationTokenValueSource : ValueSource<String, GetAuthorizationTokenValueSource.Parameters> {
    /**
     * Parameters for [GetRepositoryEndpointValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /**
         * The underlying CodeArtifact client.
         */
        val client: Property<CodeartifactClient>

        val domain: Property<String>
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
            val response = parameters.client.get().getAuthorizationToken(request)
            response.authorizationToken()
        } catch (_: CodeartifactException) {
            null
        }
    }
}
