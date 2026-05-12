package com.kelvsyc.gradle.aws.java.sts

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.sts.model.GetCallerIdentityRequest

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
        val request = GetCallerIdentityRequest.builder().build()
        val response = parameters.service.get().getClient().getCallerIdentity(request)

        return buildMap {
            response.account()?.let { put("account", it) }
            response.arn()?.let { put("arn", it) }
            response.userId()?.let { put("userId", it) }
        }
    }
}
