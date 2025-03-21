package com.kelvsyc.gradle.internal.aws.kotlin.secretsmanager

import aws.sdk.kotlin.services.secretsmanager.SecretsManagerClient
import com.kelvsyc.gradle.aws.kotlin.secretsmanager.SecretsManagerClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class SecretsManagerClientInfoInternal : SecretsManagerClientInfo, ServiceClientInfoInternal<SecretsManagerClient> {
    override fun createClient(): SecretsManagerClient {
        return SecretsManagerClient {
            if (this@SecretsManagerClientInfoInternal.region.isPresent) {
                region = this@SecretsManagerClientInfoInternal.region.get()
            }

            if (this@SecretsManagerClientInfoInternal.credentials.isPresent) {
                credentialsProvider = this@SecretsManagerClientInfoInternal.credentials.get()
            }
        }
    }
}
