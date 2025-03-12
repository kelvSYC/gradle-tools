package com.kelvsyc.gradle.internal.aws.java.sns

import com.kelvsyc.gradle.aws.java.sns.SnsAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.sns.SnsAsyncClient

abstract class SnsAsyncClientInfoInternal : SnsAsyncClientInfo, ServiceClientInfoInternal<SnsAsyncClient> {
    override fun createClient(): SnsAsyncClient {
        return SnsAsyncClient.builder().apply {
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
