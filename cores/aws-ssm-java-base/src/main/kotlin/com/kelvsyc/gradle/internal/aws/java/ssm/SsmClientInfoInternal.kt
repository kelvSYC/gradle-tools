package com.kelvsyc.gradle.internal.aws.java.ssm

import com.kelvsyc.gradle.aws.java.ssm.SsmClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.ssm.SsmClient

abstract class SsmClientInfoInternal : SsmClientInfo, ServiceClientInfoInternal<SsmClient> {
    override fun createClient(): SsmClient {
        return SsmClient.builder().apply {
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
