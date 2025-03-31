package com.kelvsyc.gradle.internal.aws.java.ses

import com.kelvsyc.gradle.aws.java.ses.SesClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.ses.SesClient

abstract class SesClientInfoInternal : SesClientInfo, ServiceClientInfoInternal<SesClient> {
    override fun createClient(): SesClient {
        return SesClient.builder().apply {
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
