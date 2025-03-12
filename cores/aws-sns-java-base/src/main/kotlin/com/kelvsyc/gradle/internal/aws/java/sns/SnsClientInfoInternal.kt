package com.kelvsyc.gradle.internal.aws.java.sns

import com.kelvsyc.gradle.aws.java.sns.SnsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.sns.SnsClient

abstract class SnsClientInfoInternal : SnsClientInfo, ServiceClientInfoInternal<SnsClient> {
    override fun createClient(): SnsClient {
        return SnsClient.builder().apply {
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
