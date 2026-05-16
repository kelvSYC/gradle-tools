package com.kelvsyc.gradle.hashicorp.vault

import io.github.jopenlibs.vault.Vault

/**
 * Test-only [VaultClientBuildService] that returns a pre-supplied mock [Vault] client.
 *
 * Set [mockClient] before the service is first accessed; the same instance is returned on every call.
 */
abstract class MockVaultClientBuildService : VaultClientBuildService() {
    override fun createClient(): Vault = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: Vault? = null
    }
}
