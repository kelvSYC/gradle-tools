package com.kelvsyc.gradle.azure.servicebus

import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * An [AbstractSendMessageBatch] task wired to a [com.azure.messaging.servicebus.ServiceBusSenderClient]
 * supplied by a [ServiceBusSenderClientBuildService].
 */
@DisableCachingByDefault(because = "Sending to an external service is not cacheable")
abstract class SendMessageBatch @Inject constructor(
    objects: ObjectFactory,
) : AbstractSendMessageBatch(objects) {

    /**
     * Build service managing the Service Bus sender client to use.
     */
    @get:ServiceReference
    abstract val service: Property<ServiceBusSenderClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
