package com.kelvsyc.gradle.azure.keyvault

import com.kelvsyc.gradle.clients.ClientsBaseExtension

/**
 * Registers a synchronous Azure Key Vault [SecretClientInfo].
 */
fun ClientsBaseExtension.registerAzureKeyVaultSecretClient(
    name: String,
    configureAction: SecretClientInfo.() -> Unit,
) = service.get().registerIfAbsent(name, configureAction)

/**
 * Registers an asynchronous Azure Key Vault [SecretAsyncClientInfo].
 */
fun ClientsBaseExtension.registerAzureKeyVaultSecretAsyncClient(
    name: String,
    configureAction: SecretAsyncClientInfo.() -> Unit,
) = service.get().registerIfAbsent(name, configureAction)
