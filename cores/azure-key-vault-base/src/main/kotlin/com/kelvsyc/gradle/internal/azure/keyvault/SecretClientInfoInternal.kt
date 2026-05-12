package com.kelvsyc.gradle.internal.azure.keyvault

import com.azure.security.keyvault.secrets.SecretClient
import com.azure.security.keyvault.secrets.SecretClientBuilder
import com.kelvsyc.gradle.azure.keyvault.SecretClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class SecretClientInfoInternal : SecretClientInfo, ServiceClientInfoInternal<SecretClient> {
    override fun createClient(): SecretClient {
        return SecretClientBuilder().apply {
            vaultUrl(vaultUrl.get())
            if (credential.isPresent) {
                credential(credential.get())
            }
        }.buildClient()
    }
}
