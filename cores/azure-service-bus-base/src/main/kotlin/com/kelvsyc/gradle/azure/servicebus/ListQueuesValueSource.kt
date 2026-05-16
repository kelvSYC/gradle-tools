package com.kelvsyc.gradle.azure.servicebus

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of queue names within an Azure Service Bus namespace.
 *
 * Pagination is handled internally by the Azure SDK.
 */
abstract class ListQueuesValueSource : ValueSource<List<String>, ListQueuesValueSource.Parameters> {
    /**
     * Parameters for [ListQueuesValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Service Bus administration client. */
        @get:Internal
        val service: Property<ServiceBusAdministrationClientBuildService>
    }

    override fun obtain(): List<String> =
        parameters.service.get().getClient().listQueues().map { it.name }
}
