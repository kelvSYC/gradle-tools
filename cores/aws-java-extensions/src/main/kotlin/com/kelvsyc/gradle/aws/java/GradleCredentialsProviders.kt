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
 */
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
