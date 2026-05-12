package com.kelvsyc.gradle.aws.java.secretsmanager

import com.amazonaws.secretsmanager.caching.SecretCache

/**
 * Test-only [SecretCacheBuildService] that returns a pre-supplied mock [SecretCache].
 *
 * The inherited [Params.baseService] is intentionally left unset; the overridden [createClient] does not
 * read it.
 */
abstract class MockSecretCacheBuildService : SecretCacheBuildService() {
    override fun createClient(): SecretCache = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: SecretCache? = null
    }
}
