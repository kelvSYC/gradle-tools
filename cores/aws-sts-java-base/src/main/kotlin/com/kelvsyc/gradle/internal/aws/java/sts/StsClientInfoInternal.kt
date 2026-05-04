package com.kelvsyc.gradle.internal.aws.java.sts

import com.kelvsyc.gradle.aws.java.sts.StsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.sts.StsClient

abstract class StsClientInfoInternal : StsClientInfo, ServiceClientInfoInternal<StsClient> {
    override fun createClient(): StsClient {
        return StsClient.builder().apply {
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
