package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation retrieving an authorization token from AWS ECR for the caller's default
 * registry.
 *
 * Returns the base64-encoded `user:password` token suitable for `docker login`. The result is the first entry
 * of [aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenResponse.authorizationData].
 */
abstract class GetAuthorizationTokenValueSource : ValueSource<String, GetAuthorizationTokenValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing ECR clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of an [EcrClientInfo]. */
        val clientName: Property<String>
    }

    private val client: Provider<EcrClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): String? {
        val request = GetAuthorizationTokenRequest {}

        return runBlocking {
            client.get().getAuthorizationToken(request)
                .authorizationData
                ?.firstOrNull()
                ?.authorizationToken
        }
    }
}
