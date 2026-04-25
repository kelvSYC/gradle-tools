package com.kelvsyc.gradle.internal.google.cloud.secretmanager

import com.google.cloud.secretmanager.v1.SecretManagerServiceClient
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import com.kelvsyc.gradle.google.cloud.secretmanager.MockSecretManagerClientInfo
import io.mockk.mockk

abstract class MockSecretManagerClientInfoInternal : MockSecretManagerClientInfo, ServiceClientInfoInternal<SecretManagerServiceClient> {
    override fun createClient(): SecretManagerServiceClient = mockk()
}
