package com.kelvsyc.gradle.internal.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.SqsClient
import com.kelvsyc.gradle.aws.kotlin.sqs.MockSqsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockSqsClientInfoInternal : MockSqsClientInfo, ServiceClientInfoInternal<SqsClient> {
    override fun createClient(): SqsClient = mockk()
}

