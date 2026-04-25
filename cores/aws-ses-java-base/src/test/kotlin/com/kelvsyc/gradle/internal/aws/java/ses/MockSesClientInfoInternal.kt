package com.kelvsyc.gradle.internal.aws.java.ses

import com.kelvsyc.gradle.aws.java.ses.MockSesClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.ses.SesClient

abstract class MockSesClientInfoInternal : MockSesClientInfo, ServiceClientInfoInternal<SesClient> {
    override fun createClient(): SesClient = mockk()
}

