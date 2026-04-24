package com.kelvsyc.gradle.internal.aws.java.s3

import com.kelvsyc.gradle.aws.java.s3.MockS3AsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.s3.S3AsyncClient

abstract class MockS3AsyncClientInfoInternal : MockS3AsyncClientInfo, ServiceClientInfoInternal<S3AsyncClient> {
    override fun createClient(): S3AsyncClient = mockk()
}
