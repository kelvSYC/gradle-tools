package com.kelvsyc.gradle.azure.servicebus

import com.azure.messaging.servicebus.ServiceBusSenderClient

/**
 * Test-only [ServiceBusSenderClientBuildService] that returns a pre-supplied mock client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockServiceBusSenderClientBuildService : ServiceBusSenderClientBuildService() {
    override fun createClient(): ServiceBusSenderClient = checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: ServiceBusSenderClient? = null
    }
}
