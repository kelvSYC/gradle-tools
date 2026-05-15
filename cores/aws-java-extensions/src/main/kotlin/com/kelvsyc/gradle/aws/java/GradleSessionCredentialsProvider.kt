package com.kelvsyc.gradle.aws.java

import org.gradle.api.provider.Provider
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider
import software.amazon.awssdk.auth.credentials.AwsSessionCredentials
import org.gradle.api.credentials.AwsCredentials as GradleAwsCredentials

/**
 * [AwsCredentialsProvider] implementation backed by a [Provider] of
 * [Gradle AwsCredentials][GradleAwsCredentials].
 *
 * The resolved credentials is of type [AwsSessionCredentials].
 *
 * **Deprecated.** [GradleAwsCredentials][org.gradle.api.credentials.AwsCredentials] is a
 * configuration-phase abstraction designed for Gradle repository authentication (dependency
 * resolution). It is not intended to supply credentials to AWS SDK clients that authenticate
 * during task execution. Prefer building an AWS build service with
 * [CredentialReference.EnvironmentVariable][com.kelvsyc.gradle.clients.CredentialReference.EnvironmentVariable]
 * or [CredentialReference.SystemProperty][com.kelvsyc.gradle.clients.CredentialReference.SystemProperty]
 * parameters instead.
 */
@Deprecated(
    "AwsCredentials (Gradle) is a configuration-phase abstraction designed for repository auth, not " +
        "for AWS SDK clients that authenticate at task execution time. Prefer a build service " +
        "configured with CredentialReference.EnvironmentVariable or CredentialReference.SystemProperty.",
    level = DeprecationLevel.WARNING,
)
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
