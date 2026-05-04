package com.kelvsyc.gradle.internal.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import com.kelvsyc.gradle.aws.kotlin.kms.MockKmsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockKmsClientInfoInternal : MockKmsClientInfo, ServiceClientInfoInternal<KmsClient> {
    override fun createClient(): KmsClient = mockk()
}
