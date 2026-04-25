package com.kelvsyc.gradle.internal.aws.java.sqs

import com.kelvsyc.gradle.aws.java.sqs.MockSqsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.sqs.SqsClient

abstract class MockSqsClientInfoInternal : MockSqsClientInfo, ServiceClientInfoInternal<SqsClient> {
    override fun createClient(): SqsClient = mockk()
}

