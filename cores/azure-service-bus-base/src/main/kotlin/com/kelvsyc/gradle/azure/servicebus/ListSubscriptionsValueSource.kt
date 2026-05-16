package com.kelvsyc.gradle.azure.servicebus

import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of subscription names for a given Azure Service Bus topic.
 *
 * Pagination is handled internally by the Azure SDK.
 */
abstract class ListSubscriptionsValueSource :
    ValueSource<List<String>, ListSubscriptionsValueSource.Parameters> {

    /**
     * Parameters for [ListSubscriptionsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the Service Bus administration client. */
        @get:Internal
        val service: Property<ServiceBusAdministrationClientBuildService>

        /** The topic name (short name, not the full resource path). */
        @get:Input
        val topicName: Property<String>
    }

    override fun obtain(): List<String> =
        parameters.service.get().getClient()
            .listSubscriptions(parameters.topicName.get())
            .map { it.subscriptionName }
}
