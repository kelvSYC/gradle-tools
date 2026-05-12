package com.kelvsyc.gradle.azure.keyvault

import com.azure.security.keyvault.secrets.SecretClient
import com.kelvsyc.gradle.clients.ServiceClientInfo

interface MockSecretClientInfo : ServiceClientInfo<SecretClient>
