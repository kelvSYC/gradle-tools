package com.kelvsyc.gradle.internal.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import com.kelvsyc.gradle.aws.kotlin.s3.MockS3ClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockS3ClientInfoInternal : MockS3ClientInfo, ServiceClientInfoInternal<S3Client> {
    override fun createClient(): S3Client = mockk()
}
