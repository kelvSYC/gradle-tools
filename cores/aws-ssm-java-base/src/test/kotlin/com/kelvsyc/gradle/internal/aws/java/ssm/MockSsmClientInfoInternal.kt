package com.kelvsyc.gradle.internal.aws.java.ssm

import com.kelvsyc.gradle.aws.java.ssm.MockSsmClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.ssm.SsmClient

abstract class MockSsmClientInfoInternal : MockSsmClientInfo, ServiceClientInfoInternal<SsmClient> {
    override fun createClient(): SsmClient = mockk()
}
