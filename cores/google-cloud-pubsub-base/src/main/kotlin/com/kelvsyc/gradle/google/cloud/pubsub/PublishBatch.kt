package com.kelvsyc.gradle.google.cloud.pubsub

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.plugins.ClientsBasePlugin
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.api.tasks.Internal
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * An [AbstractPublishBatch] task wired to a [TopicAdminClient][com.google.cloud.pubsub.v1.TopicAdminClient]
 * registered in [ClientsBaseService].
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class PublishBatch @Inject constructor(
    objects: ObjectFactory,
) : AbstractPublishBatch(objects) {
    /** The shared [ClientsBaseService] holding the registered Pub/Sub client. */
    @get:ServiceReference(ClientsBasePlugin.EXTENSION_NAME)
    abstract val service: Property<ClientsBaseService>

    /** Registered name of a [PubSubClientInfo]. */
    @get:Internal
    abstract val clientName: Property<String>
}
