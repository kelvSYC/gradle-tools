package com.kelvsyc.gradle.internal.azure.keyvault

import com.azure.security.keyvault.secrets.SecretAsyncClient
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.kelvsyc.gradle.azure.keyvault.SecretAsyncClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class SecretAsyncClientInfoInternal : SecretAsyncClientInfo, ServiceClientInfoInternal<SecretAsyncClient> {
    override fun createClient(): SecretAsyncClient {
        return SecretClientBuilder().apply {
            vaultUrl(vaultUrl.get())
            if (credential.isPresent) {
                credential(credential.get())
            }
        }.buildAsyncClient()
    }
}
