package com.kelvsyc.gradle.internal.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.SnsClient
import com.kelvsyc.gradle.aws.kotlin.sns.MockSnsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockSnsClientInfoInternal : MockSnsClientInfo, ServiceClientInfoInternal<SnsClient> {
    override fun createClient(): SnsClient = mockk()
}
