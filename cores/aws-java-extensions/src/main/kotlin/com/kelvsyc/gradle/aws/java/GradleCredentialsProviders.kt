package com.kelvsyc.gradle.aws.java

import org.gradle.api.credentials.PasswordCredentials
import org.gradle.api.provider.Provider
import software.amazon.awssdk.auth.credentials.AwsBasicCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentials
import software.amazon.awssdk.auth.credentials.AwsCredentialsProvider

/**
 * [AwsCredentialsProvider] implementation backed by a [Provider] of [PasswordCredentials].
 *
 * The resolved credentials is of type [AwsBasicCredentials].
 *
 * **Deprecated.** [PasswordCredentials] is a configuration-phase abstraction designed for Gradle
 * repository authentication (dependency resolution). It is not intended to supply credentials to
 * AWS SDK clients that authenticate during task execution. Prefer building an AWS build service
 * with [CredentialReference.EnvironmentVariable][com.kelvsyc.gradle.clients.CredentialReference.EnvironmentVariable]
 * or [CredentialReference.SystemProperty][com.kelvsyc.gradle.clients.CredentialReference.SystemProperty]
 * parameters instead.
 */
@Deprecated(
    "PasswordCredentials is a configuration-phase abstraction designed for repository auth, not " +
        "for AWS SDK clients that authenticate at task execution time. Prefer a build service " +
        "configured with CredentialReference.EnvironmentVariable or CredentialReference.SystemProperty.",
    level = DeprecationLevel.WARNING,
)
class GradleCredentialsProviders(credentials: Provider<PasswordCredentials>) : AwsCredentialsProvider {
    private val credentialsInternal = credentials.map {
        AwsBasicCredentials.builder().apply {
            accessKeyId(it.username)
            secretAccessKey(it.password)
        }.build()
    }

    override fun resolveCredentials(): AwsCredentials {
        return credentialsInternal.get()
    }
}
