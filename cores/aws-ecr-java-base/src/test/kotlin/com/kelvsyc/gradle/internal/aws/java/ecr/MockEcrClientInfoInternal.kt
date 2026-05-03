package com.kelvsyc.gradle.internal.aws.java.ecr

import com.kelvsyc.gradle.aws.java.ecr.MockEcrClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.ecr.EcrClient

abstract class MockEcrClientInfoInternal : MockEcrClientInfo, ServiceClientInfoInternal<EcrClient> {
    override fun createClient(): EcrClient = mockk()
}
