package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.model.GetCallerIdentityRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation that returns the calling identity for the configured client as a [Map] with the
 * keys `account`, `arn`, and `userId`.
 *
 * Useful for diagnostics and for asserting that a build is running under the expected AWS principal.
 */
abstract class GetCallerIdentityValueSource :
    ValueSource<Map<String, String>, GetCallerIdentityValueSource.Parameters> {
    /**
     * Parameters for [GetCallerIdentityValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the STS client. */
        val service: Property<StsClientBuildService>
    }

    override fun obtain(): Map<String, String>? {
        val request = GetCallerIdentityRequest {}
        val client = parameters.service.get().getClient()

        return runBlocking {
            val response = client.getCallerIdentity(request)
            buildMap {
                response.account?.let { put("account", it) }
                response.arn?.let { put("arn", it) }
                response.userId?.let { put("userId", it) }
            }
        }
    }
}
