package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import aws.sdk.kotlin.services.sts.model.GetCallerIdentityRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation that returns the calling identity for the configured client as a [Map] with the
 * keys `account`, `arn`, and `userId`.
 *
 * Useful for diagnostics and for asserting that a build is running under the expected AWS principal.
 */
abstract class GetCallerIdentityValueSource : ValueSource<Map<String, String>, GetCallerIdentityValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing STS clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of an [StsClientInfo]. */
        val clientName: Property<String>
    }

    private val client: Provider<StsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): Map<String, String>? {
        val request = GetCallerIdentityRequest {}

        return runBlocking {
            val response = client.get().getCallerIdentity(request)
            buildMap {
                response.account?.let { put("account", it) }
                response.arn?.let { put("arn", it) }
                response.userId?.let { put("userId", it) }
            }
        }
    }
}
