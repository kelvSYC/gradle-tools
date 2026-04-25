package com.kelvsyc.gradle.internal.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import com.kelvsyc.gradle.aws.kotlin.ses.MockSesClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockSesClientInfoInternal : MockSesClientInfo, ServiceClientInfoInternal<SesClient> {
    override fun createClient(): SesClient = mockk()
}
