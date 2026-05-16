package com.kelvsyc.gradle.azure.servicebus

import com.azure.messaging.servicebus.administration.ServiceBusAdministrationClient

/**
 * Test-only [ServiceBusAdministrationClientBuildService] that returns a pre-supplied mock client.
 *
 * Set [mockClient] before retrieving the client; the same instance is returned on every call.
 */
abstract class MockServiceBusAdministrationClientBuildService : ServiceBusAdministrationClientBuildService() {
    override fun createClient(): ServiceBusAdministrationClient =
        checkNotNull(mockClient) { "mockClient not set" }

    companion object {
        var mockClient: ServiceBusAdministrationClient? = null
    }
}
