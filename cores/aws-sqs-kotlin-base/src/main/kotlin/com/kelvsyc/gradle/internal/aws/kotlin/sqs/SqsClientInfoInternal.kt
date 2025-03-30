package com.kelvsyc.gradle.internal.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.SqsClient
import com.kelvsyc.gradle.aws.kotlin.sqs.SqsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class SqsClientInfoInternal : SqsClientInfo, ServiceClientInfoInternal<SqsClient> {
    override fun createClient(): SqsClient {
        return SqsClient {
            if (this@SqsClientInfoInternal.region.isPresent) {
                region = this@SqsClientInfoInternal.region.get()
            }

            if (this@SqsClientInfoInternal.credentials.isPresent) {
                credentialsProvider = this@SqsClientInfoInternal.credentials.get()
            }
        }
    }
}
