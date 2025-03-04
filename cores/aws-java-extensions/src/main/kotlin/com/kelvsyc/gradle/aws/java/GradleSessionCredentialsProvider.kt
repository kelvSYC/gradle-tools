package com.kelvsyc.gradle.aws.java

import org.gradle.api.provider.Provider
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import org.gradle.api.credentials.AwsCredentials as GradleAwsCredentials

/**
 * [AwsCredentialsProvider] implementation backed by a [Provider] of [Gradle AwsCredentials][GradleAwsCredentials].
 *
 * The resolved credentials is of type [AwsSessionCredentials].
 */
class GradleSessionCredentialsProvider(credentials: Provider<GradleAwsCredentials>) : AwsCredentialsProvider {
    private val credentialsInternal = credentials.map {
        AwsSessionCredentials.builder().apply {
            accessKeyId(it.accessKey)
            secretAccessKey(it.secretKey)
            sessionToken(it.sessionToken)
        }.build()
    }

    override fun resolveCredentials(): AwsCredentials {
        return credentialsInternal.get()
    }
}
