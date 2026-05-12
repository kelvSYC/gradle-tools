package com.kelvsyc.gradle.internal.azure.keyvault

import com.azure.security.keyvault.secrets.SecretClient
import com.kelvsyc.gradle.azure.keyvault.MockSecretClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockSecretClientInfoInternal : MockSecretClientInfo, ServiceClientInfoInternal<SecretClient> {
    override fun createClient(): SecretClient = mockk()
}
