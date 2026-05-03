package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetAuthorizationTokenRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

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
        val request = GetAuthorizationTokenRequest {
            domain = parameters.domain.get()
            domainOwner = parameters.domainOwner.get()

            durationSeconds = parameters.duration.get()
        }

        return runBlocking {
            client.get().getAuthorizationToken(request).authorizationToken
        }
    }
}
