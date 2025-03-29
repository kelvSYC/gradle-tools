package com.kelvsyc.gradle.internal.aws.java.sqs

import com.kelvsyc.gradle.aws.java.sqs.SqsAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.sqs.SqsAsyncClient

abstract class SqsAsyncClientInfoInternal : SqsAsyncClientInfo, ServiceClientInfoInternal<SqsAsyncClient> {
    override fun createClient(): SqsAsyncClient {
        return SqsAsyncClient.builder().apply {
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
