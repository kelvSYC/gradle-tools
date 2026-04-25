package com.kelvsyc.gradle.internal.aws.java.ses

import com.kelvsyc.gradle.aws.java.ses.MockSesAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.ses.SesAsyncClient

abstract class MockSesAsyncClientInfoInternal : MockSesAsyncClientInfo, ServiceClientInfoInternal<SesAsyncClient> {
    override fun createClient(): SesAsyncClient = mockk()
}

