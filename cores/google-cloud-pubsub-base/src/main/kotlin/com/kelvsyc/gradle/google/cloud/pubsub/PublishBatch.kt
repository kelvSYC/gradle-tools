package com.kelvsyc.gradle.google.cloud.pubsub

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault

/**
 * An [AbstractPublishBatch] task wired to a [TopicAdminClient][com.google.cloud.pubsub.v1.TopicAdminClient]
 * supplied by a [TopicAdminClientBuildService].
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class PublishBatch : AbstractPublishBatch() {
    /**
     * Build service managing the Pub/Sub client to use.
     */
    @get:ServiceReference
    abstract val service: Property<TopicAdminClientBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
