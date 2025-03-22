package com.kelvsyc.gradle.aws.kotlin

import aws.sdk.kotlin.runtime.auth.credentials.StaticCredentialsProvider
import aws.smithy.kotlin.runtime.auth.awscredentials.Credentials
import aws.smithy.kotlin.runtime.auth.awscredentials.CredentialsProvider
import org.gradle.api.credentials.AwsCredentials
import org.gradle.api.provider.Provider

val Provider<AwsCredentials>.asCredentialsProvider: Provider<CredentialsProvider>
    get() = map {
        StaticCredentialsProvider(Credentials(it.accessKey!!, it.secretKey!!, it.sessionToken))
    }

val Provider<Credentials>.asGradleCredentials: Provider<AwsCredentials>
    get() = map {
        object : AwsCredentials {
            override fun getAccessKey() = it.accessKeyId
            override fun getSecretKey() = it.secretAccessKey
            override fun getSessionToken() = it.sessionToken

            override fun setAccessKey(accessKey: String?) = throw UnsupportedOperationException("Credentials are not mutable")
            override fun setSecretKey(secretKey: String?) = throw UnsupportedOperationException("Credentials are not mutable")
            override fun setSessionToken(token: String?) = throw UnsupportedOperationException("Credentials are not mutable")
        }
    }
