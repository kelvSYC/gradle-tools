package com.kelvsyc.gradle.internal.aws.java.secretsmanager

import com.kelvsyc.gradle.aws.java.secretsmanager.SecretsManagerAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.secretsmanager.SecretsManagerAsyncClient

abstract class SecretsManagerAsyncClientInfoInternal : SecretsManagerAsyncClientInfo, ServiceClientInfoInternal<SecretsManagerAsyncClient> {
    override fun createClient(): SecretsManagerAsyncClient {
        return SecretsManagerAsyncClient.builder().apply {
            if (region.isPresent) {
                region(region.get())
            }
            if (credentials.isPresent) {
                credentialsProvider(credentials.get())
            } else {
                credentialsProvider(AnonymousCredentialsProvider.create())
            }
        }.build()
    }
}
