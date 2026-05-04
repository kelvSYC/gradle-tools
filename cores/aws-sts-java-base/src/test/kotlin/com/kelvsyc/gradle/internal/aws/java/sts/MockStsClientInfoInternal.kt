package com.kelvsyc.gradle.internal.aws.java.sts

import com.kelvsyc.gradle.aws.java.sts.MockStsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.sts.StsClient

abstract class MockStsClientInfoInternal : MockStsClientInfo, ServiceClientInfoInternal<StsClient> {
    override fun createClient(): StsClient = mockk()
}
