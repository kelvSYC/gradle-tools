package com.kelvsyc.gradle.aws.java.codeartifact

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.codeartifact.CodeartifactClient
import software.amazon.awssdk.services.codeartifact.model.CodeartifactException
import software.amazon.awssdk.services.codeartifact.model.GetAuthorizationTokenRequest
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation retrieving an authorization token from AWS CodeArtifact.
 *
 * The value is obtained from a request to AWS CodeArtifact.
 */
abstract class GetAuthorizationTokenValueSource : ValueSource<String, GetAuthorizationTokenValueSource.Parameters> {
    /**
     * Parameters for [GetAuthorizationTokenValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing CodeArtifact clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [CodeArtifactClientInfo]. */
        val clientName: Property<String>

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

    private val client: Provider<CodeartifactClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): String? {
        val request = GetAuthorizationTokenRequest.builder().apply {
            domain(parameters.domain.get())
            domainOwner(parameters.domainOwner.get())

            durationSeconds(parameters.duration.get())
        }.build()

        return try {
            val response = client.get().getAuthorizationToken(request)
            response.authorizationToken()
        } catch (_: CodeartifactException) {
            null
        }
    }
}
