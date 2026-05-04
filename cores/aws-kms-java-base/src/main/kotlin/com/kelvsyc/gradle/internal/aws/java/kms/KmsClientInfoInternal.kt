package com.kelvsyc.gradle.internal.aws.java.kms

import com.kelvsyc.gradle.aws.java.kms.KmsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.kms.KmsClient

abstract class KmsClientInfoInternal : KmsClientInfo, ServiceClientInfoInternal<KmsClient> {
    override fun createClient(): KmsClient {
        return KmsClient.builder().apply {
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
