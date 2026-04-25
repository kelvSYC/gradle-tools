package com.kelvsyc.gradle.internal.aws.java.sns

import com.kelvsyc.gradle.aws.java.sns.MockSnsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.sns.SnsClient

abstract class MockSnsClientInfoInternal : MockSnsClientInfo, ServiceClientInfoInternal<SnsClient> {
    override fun createClient(): SnsClient = mockk()
}
