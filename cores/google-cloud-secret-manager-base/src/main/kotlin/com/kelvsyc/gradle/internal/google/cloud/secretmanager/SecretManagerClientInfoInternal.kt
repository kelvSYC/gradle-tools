package com.kelvsyc.gradle.internal.google.cloud.secretmanager

import com.google.api.gax.core.FixedCredentialsProvider
import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.google.cloud.secretmanager.v1.SecretManagerServiceSettings
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import com.kelvsyc.gradle.google.cloud.secretmanager.SecretManagerClientInfo

abstract class SecretManagerClientInfoInternal : SecretManagerClientInfo, ServiceClientInfoInternal<SecretManagerServiceClient> {
    override fun createClient(): SecretManagerServiceClient {
        val settings = SecretManagerServiceSettings.newBuilder().apply {
            if (credentials.isPresent) {
                credentialsProvider = FixedCredentialsProvider.create(credentials.get())
            }
        }.build()
        return SecretManagerServiceClient.create(settings)
    }
}
