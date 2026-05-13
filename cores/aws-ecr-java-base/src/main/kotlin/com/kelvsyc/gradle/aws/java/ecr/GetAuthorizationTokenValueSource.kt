package com.kelvsyc.gradle.aws.java.ecr

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenRequest

/**
 * [ValueSource] implementation retrieving an authorization token from AWS ECR for the caller's default
 * registry.
 *
 * Returns the base64-encoded `user:password` token suitable for `docker login`. The result is the first entry
 * of [software.amazon.awssdk.services.ecr.model.GetAuthorizationTokenResponse.authorizationData].
 */
abstract class GetAuthorizationTokenValueSource : ValueSource<String, GetAuthorizationTokenValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The build service managing the ECR client. */
        @get:Internal
        val service: Property<EcrClientBuildService>
    }

    override fun obtain(): String? {
        val request = GetAuthorizationTokenRequest.builder().build()

        return parameters.service.get().getClient()
            .getAuthorizationToken(request)
            .authorizationData()
            .firstOrNull()
            ?.authorizationToken()
    }
}
