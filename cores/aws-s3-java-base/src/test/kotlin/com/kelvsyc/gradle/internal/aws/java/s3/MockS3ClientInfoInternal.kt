package com.kelvsyc.gradle.internal.aws.java.s3

import com.kelvsyc.gradle.aws.java.s3.MockS3ClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.s3.S3Client

abstract class MockS3ClientInfoInternal : MockS3ClientInfo, ServiceClientInfoInternal<S3Client> {
    override fun createClient(): S3Client = mockk()
}
