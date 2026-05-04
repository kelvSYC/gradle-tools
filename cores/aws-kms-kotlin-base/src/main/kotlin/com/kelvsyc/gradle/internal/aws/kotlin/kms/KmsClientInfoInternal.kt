package com.kelvsyc.gradle.internal.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import com.kelvsyc.gradle.aws.kotlin.kms.KmsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class KmsClientInfoInternal : KmsClientInfo, ServiceClientInfoInternal<KmsClient> {
    override fun createClient(): KmsClient {
        return KmsClient {
            if (this@KmsClientInfoInternal.region.isPresent) {
                region = this@KmsClientInfoInternal.region.get()
            }

            if (this@KmsClientInfoInternal.credentials.isPresent) {
                credentialsProvider = this@KmsClientInfoInternal.credentials.get()
            }
        }
    }
}
