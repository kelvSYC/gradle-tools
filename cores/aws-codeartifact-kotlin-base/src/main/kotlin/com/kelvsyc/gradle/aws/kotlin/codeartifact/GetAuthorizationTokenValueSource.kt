package com.kelvsyc.gradle.aws.kotlin.codeartifact

import aws.sdk.kotlin.services.codeartifact.CodeartifactClient
import aws.sdk.kotlin.services.codeartifact.model.GetAuthorizationTokenRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

abstract class GetAuthorizationTokenValueSource : ValueSource<String, GetAuthorizationTokenValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val domain: Property<String>
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
