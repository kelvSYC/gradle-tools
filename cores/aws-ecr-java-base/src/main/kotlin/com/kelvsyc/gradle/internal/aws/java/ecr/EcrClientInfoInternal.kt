package com.kelvsyc.gradle.internal.aws.java.ecr

import com.kelvsyc.gradle.aws.java.ecr.EcrClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.ecr.EcrClient

abstract class EcrClientInfoInternal : EcrClientInfo, ServiceClientInfoInternal<EcrClient> {
    override fun createClient(): EcrClient {
        return EcrClient.builder().apply {
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
