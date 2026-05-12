package com.kelvsyc.gradle.azure.keyvault

import com.azure.security.keyvault.secrets.SecretAsyncClient

/**
 * Configuration for an asynchronous [SecretAsyncClient] registration.
 */
interface SecretAsyncClientInfo : AzureKeyVaultClientInfo<SecretAsyncClient>
