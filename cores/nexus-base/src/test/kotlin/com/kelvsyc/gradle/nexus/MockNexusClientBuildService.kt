package com.kelvsyc.gradle.nexus

/**
 * Test-only [NexusClientBuildService] that returns a pre-supplied mock [NexusService].
 */
abstract class MockNexusClientBuildService : NexusClientBuildService() {
    override fun createClient(): NexusService = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: NexusService? = null
    }
}
