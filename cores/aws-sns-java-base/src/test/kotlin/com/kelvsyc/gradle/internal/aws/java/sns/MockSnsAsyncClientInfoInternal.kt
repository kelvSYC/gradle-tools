package com.kelvsyc.gradle.internal.aws.java.sns

import com.kelvsyc.gradle.aws.java.sns.MockSnsAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.sns.SnsAsyncClient

abstract class MockSnsAsyncClientInfoInternal : MockSnsAsyncClientInfo, ServiceClientInfoInternal<SnsAsyncClient> {
    override fun createClient(): SnsAsyncClient = mockk()
}
