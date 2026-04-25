package com.kelvsyc.gradle.internal.aws.java.secretsmanager

import com.amazonaws.secretsmanager.caching.SecretCache
import com.kelvsyc.gradle.aws.java.secretsmanager.MockSecretCacheClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk

abstract class MockSecretCacheClientInfoInternal : MockSecretCacheClientInfo, ServiceClientInfoInternal<SecretCache> {
    override fun createClient(): SecretCache = mockk()
}

