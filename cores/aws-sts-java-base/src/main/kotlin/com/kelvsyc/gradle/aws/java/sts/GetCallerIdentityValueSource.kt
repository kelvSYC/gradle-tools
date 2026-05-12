package com.kelvsyc.gradle.aws.java.sts

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that returns the calling identity for the configured client as a [Map] with the
 * keys `account`, `arn`, and `userId`.
 *
 * Useful for diagnostics and for asserting that a build is running under the expected AWS principal.
 */
abstract class GetCallerIdentityValueSource : ValueSource<Map<String, String>, GetCallerIdentityValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing STS clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of an [StsClientInfo]. */
        val clientName: Property<String>
    }

    private val client: Provider<StsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): Map<String, String>? {
        val request = GetCallerIdentityRequest.builder().build()
        val response = client.get().getCallerIdentity(request)

        return buildMap {
            response.account()?.let { put("account", it) }
            response.arn()?.let { put("arn", it) }
            response.userId()?.let { put("userId", it) }
        }
    }
}
