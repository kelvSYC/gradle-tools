package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.pubsub.v1.ListTopicSubscriptionsRequest
import com.google.pubsub.v1.TopicName
import org.gradle.api.provider.Property
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
        /** The build service managing the Pub/Sub client. */
        @get:Internal
        val service: Property<TopicAdminClientBuildService>

        /** GCP project ID containing the topic. */
        val projectId: Property<String>

        /** The topic ID (short name, not the full resource name). */
        val topicId: Property<String>
    }

    override fun obtain(): List<String>? {
        val topicName = TopicName.of(parameters.projectId.get(), parameters.topicId.get()).toString()
        val request = ListTopicSubscriptionsRequest.newBuilder().apply {
            topic = topicName
        }.build()
        return parameters.service.get().getClient().listTopicSubscriptions(request).iterateAll().toList()
    }
}
