package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.pubsub.v1.ListTopicSubscriptionsRequest
import com.google.pubsub.v1.TopicName
import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation providing a list of subscription resource names for a given Pub/Sub topic.
 *
 * Pagination is handled internally via the high-level paged API.
 *
 * Each entry is the fully-qualified resource name in the form
 * `projects/{project}/subscriptions/{subscription}`.
 */
abstract class ListTopicSubscriptionsValueSource :
    ValueSource<List<String>, ListTopicSubscriptionsValueSource.Parameters> {
    /**
     * Parameters for [ListTopicSubscriptionsValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing Pub/Sub clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [PubSubClientInfo]. */
        val clientName: Property<String>

        /** GCP project ID containing the topic. */
        val projectId: Property<String>

        /** The topic ID (short name, not the full resource name). */
        val topicId: Property<String>
    }

    private val client: Provider<TopicAdminClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): List<String>? {
        val topicName = TopicName.of(parameters.projectId.get(), parameters.topicId.get()).toString()
        val request = ListTopicSubscriptionsRequest.newBuilder().apply {
            topic = topicName
        }.build()
        return client.get().listTopicSubscriptions(request).iterateAll().toList()
    }
}
