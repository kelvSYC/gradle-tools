package com.kelvsyc.gradle.google.cloud

import com.google.auth.Credentials

/**
 * Test-only concrete subclass of [AbstractGcpClientBuildService] that exposes
 * [resolveCredentials] for direct assertion in unit tests.
 */
abstract class TestGcpClientBuildService : AbstractGcpClientBuildService<Unit, GcpBuildServiceParams>() {
    override fun createClient() = Unit

    fun testResolveCredentials(): Credentials? = resolveCredentials()
}
