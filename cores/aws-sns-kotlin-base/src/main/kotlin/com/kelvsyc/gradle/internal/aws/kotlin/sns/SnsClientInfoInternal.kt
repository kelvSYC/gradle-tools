package com.kelvsyc.gradle.internal.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.SnsClient
import com.kelvsyc.gradle.aws.kotlin.sns.SnsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class SnsClientInfoInternal : SnsClientInfo, ServiceClientInfoInternal<SnsClient> {
    override fun createClient(): SnsClient {
        return SnsClient {
            if (this@SnsClientInfoInternal.region.isPresent) {
                region = this@SnsClientInfoInternal.region.get()
            }

            if (this@SnsClientInfoInternal.credentials.isPresent) {
                credentialsProvider = this@SnsClientInfoInternal.credentials.get()
            }
        }
    }
}
