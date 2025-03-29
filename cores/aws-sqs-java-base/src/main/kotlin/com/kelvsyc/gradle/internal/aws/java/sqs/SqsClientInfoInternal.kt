package com.kelvsyc.gradle.internal.aws.java.sqs

import com.kelvsyc.gradle.aws.java.sqs.SqsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.sqs.SqsClient

abstract class SqsClientInfoInternal : SqsClientInfo, ServiceClientInfoInternal<SqsClient> {
    override fun createClient(): SqsClient {
        return SqsClient.builder().apply {
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
