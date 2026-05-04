package com.kelvsyc.gradle.internal.aws.java.kms

import com.kelvsyc.gradle.aws.java.kms.MockKmsClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.services.kms.KmsClient

abstract class MockKmsClientInfoInternal : MockKmsClientInfo, ServiceClientInfoInternal<KmsClient> {
    override fun createClient(): KmsClient = mockk()
}
