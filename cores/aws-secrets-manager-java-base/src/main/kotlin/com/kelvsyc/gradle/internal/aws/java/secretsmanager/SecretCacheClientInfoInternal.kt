package com.kelvsyc.gradle.internal.aws.java.secretsmanager

import com.amazonaws.secretsmanager.caching.SecretCache
import com.amazonaws.secretsmanager.caching.SecretCacheConfiguration
import com.kelvsyc.gradle.aws.java.secretsmanager.SecretCacheClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal

abstract class SecretCacheClientInfoInternal : SecretCacheClientInfo, ServiceClientInfoInternal<SecretCache> {
    override fun createClient(): SecretCache {
        val configuration = SecretCacheConfiguration().apply {
            client = baseClient.get()

            if (this@SecretCacheClientInfoInternal.maxCacheSize.isPresent) {
                maxCacheSize = this@SecretCacheClientInfoInternal.maxCacheSize.get()
            }
            if (this@SecretCacheClientInfoInternal.cacheItemTtl.isPresent) {
                cacheItemTTL = this@SecretCacheClientInfoInternal.cacheItemTtl.get()
            }
        }
        return SecretCache(configuration)
    }
}
