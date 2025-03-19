package com.kelvsyc.gradle.internal.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import com.kelvsyc.gradle.aws.kotlin.s3.S3ClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class S3ClientInfoInternal : S3ClientInfo, ServiceClientInfoInternal<S3Client> {
    override fun createClient(): S3Client {
        return S3Client {
            if (this@S3ClientInfoInternal.region.isPresent) {
                region = this@S3ClientInfoInternal.region.get()
            }

            if (this@S3ClientInfoInternal.credentials.isPresent) {
                credentialsProvider = this@S3ClientInfoInternal.credentials.get()
            }
        }
    }
}
