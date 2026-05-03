package com.kelvsyc.gradle.internal.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import com.kelvsyc.gradle.aws.kotlin.ssm.MockSsmClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockSsmClientInfoInternal : MockSsmClientInfo, ServiceClientInfoInternal<SsmClient> {
    override fun createClient(): SsmClient = mockk()
}
