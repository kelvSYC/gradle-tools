package com.kelvsyc.gradle.internal.aws.java.ses

import com.kelvsyc.gradle.aws.java.ses.SesAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.ses.SesAsyncClient

abstract class SesAsyncClientInfoInternal : SesAsyncClientInfo, ServiceClientInfoInternal<SesAsyncClient> {
    override fun createClient(): SesAsyncClient {
        return SesAsyncClient.builder().apply {
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
