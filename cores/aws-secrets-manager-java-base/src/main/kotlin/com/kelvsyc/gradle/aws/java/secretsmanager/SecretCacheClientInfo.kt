package com.kelvsyc.gradle.aws.java.secretsmanager

import com.amazonaws.secretsmanager.caching.SecretCache
import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property
import software.amazon.awssdk.services.secretsmanager.SecretsManagerClient

interface SecretCacheClientInfo : ServiceClientInfo<SecretCache> {
    val baseClient: Property<SecretsManagerClient>

    val maxCacheSize: Property<Int>
    val cacheItemTtl: Property<Long>
}
