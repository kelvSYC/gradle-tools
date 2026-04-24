package com.kelvsyc.gradle.internal.aws.kotlin.imds

import aws.sdk.kotlin.runtime.config.imds.ImdsClient
import com.kelvsyc.gradle.aws.kotlin.imds.MockImdsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockImdsClientInfoInternal : MockImdsClientInfo, ServiceClientInfoInternal<ImdsClient> {
    override fun createClient(): ImdsClient = mockk()
}
