package com.kelvsyc.gradle.internal.aws.java.secretsmanager

import com.kelvsyc.gradle.aws.java.secretsmanager.SecretsManagerClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient

abstract class SecretsManagerClientInfoInternal : SecretsManagerClientInfo, ServiceClientInfoInternal<SecretsManagerClient> {
    override fun createClient(): SecretsManagerClient {
        return SecretsManagerClient.builder().apply {
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
