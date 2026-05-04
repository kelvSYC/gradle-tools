package com.kelvsyc.gradle.internal.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import com.kelvsyc.gradle.aws.kotlin.ecr.MockEcrClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockEcrClientInfoInternal : MockEcrClientInfo, ServiceClientInfoInternal<EcrClient> {
    override fun createClient(): EcrClient = mockk()
}
