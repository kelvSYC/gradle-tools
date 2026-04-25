package com.kelvsyc.gradle.internal.aws.java.sqs

import com.kelvsyc.gradle.aws.java.sqs.MockSqsAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.sqs.SqsAsyncClient

abstract class MockSqsAsyncClientInfoInternal : MockSqsAsyncClientInfo, ServiceClientInfoInternal<SqsAsyncClient> {
    override fun createClient(): SqsAsyncClient = mockk()
}

