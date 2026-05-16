package com.kelvsyc.gradle.azure.servicebus

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of topic names within an Azure Service Bus namespace.
 *
 * Pagination is handled internally by the Azure SDK.
 */
abstract class ListTopicsValueSource : ValueSource<List<String>, ListTopicsValueSource.Parameters> {
    /**
     * Parameters for [ListTopicsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Service Bus administration client. */
        @get:Internal
        val service: Property<ServiceBusAdministrationClientBuildService>
    }

    override fun obtain(): List<String> =
        parameters.service.get().getClient().listTopics().map { it.name }
}
