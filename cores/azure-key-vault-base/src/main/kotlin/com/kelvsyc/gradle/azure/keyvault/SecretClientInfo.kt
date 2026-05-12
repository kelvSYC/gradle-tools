package com.kelvsyc.gradle.azure.keyvault

import com.azure.security.keyvault.secrets.SecretClient

/**
 * Configuration for a synchronous [SecretClient] registration.
 */
interface SecretClientInfo : AzureKeyVaultClientInfo<SecretClient>
