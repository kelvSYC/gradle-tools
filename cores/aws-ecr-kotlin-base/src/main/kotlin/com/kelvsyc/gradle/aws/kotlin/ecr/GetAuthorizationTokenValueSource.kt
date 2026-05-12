package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation retrieving an authorization token from AWS ECR for the caller's default
 * registry.
 *
 * Returns the base64-encoded `user:password` token suitable for `docker login`. The result is the first entry
 * of [aws.sdk.kotlin.services.ecr.model.GetAuthorizationTokenResponse.authorizationData].
 */
abstract class GetAuthorizationTokenValueSource : ValueSource<String, GetAuthorizationTokenValueSource.Parameters> {
    /**
     * Parameters for [GetAuthorizationTokenValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the ECR client. */
        val service: Property<EcrClientBuildService>
    }

    override fun obtain(): String? {
        val request = GetAuthorizationTokenRequest {}

        return runBlocking {
            parameters.service.get().getClient().getAuthorizationToken(request)
                .authorizationData
                ?.firstOrNull()
                ?.authorizationToken
        }
    }
}
