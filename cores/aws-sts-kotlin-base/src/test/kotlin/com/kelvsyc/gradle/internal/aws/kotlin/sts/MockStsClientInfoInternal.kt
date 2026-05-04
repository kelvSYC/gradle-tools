package com.kelvsyc.gradle.internal.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import com.kelvsyc.gradle.aws.kotlin.sts.MockStsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockStsClientInfoInternal : MockStsClientInfo, ServiceClientInfoInternal<StsClient> {
    override fun createClient(): StsClient = mockk()
}
