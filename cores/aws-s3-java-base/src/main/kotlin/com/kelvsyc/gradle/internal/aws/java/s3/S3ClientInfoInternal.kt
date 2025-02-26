package com.kelvsyc.gradle.internal.aws.java.s3

import com.kelvsyc.gradle.aws.java.s3.S3ClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.auth.credentials.AnonymousCredentialsProvider
import software.amazon.awssdk.services.s3.S3Client

abstract class S3ClientInfoInternal : S3ClientInfo, ServiceClientInfoInternal<S3Client> {
    override fun createClient(): S3Client {
        return S3Client.builder().apply {
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
