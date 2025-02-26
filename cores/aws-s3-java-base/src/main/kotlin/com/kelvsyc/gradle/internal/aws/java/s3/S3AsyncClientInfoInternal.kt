package com.kelvsyc.gradle.internal.aws.java.s3

import com.kelvsyc.gradle.aws.java.s3.S3AsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.s3.S3AsyncClient

abstract class S3AsyncClientInfoInternal : S3AsyncClientInfo, ServiceClientInfoInternal<S3AsyncClient> {
    override fun createClient(): S3AsyncClient {
        return S3AsyncClient.crtBuilder().apply {
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
