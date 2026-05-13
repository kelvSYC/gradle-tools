package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation publishing a single message to a Google Cloud Pub/Sub topic.
 *
 * For topics with message ordering enabled, set [Parameters.orderingKey].
 */
abstract class PublishAction : WorkAction<PublishAction.Parameters> {
    /**
     * Parameters for [PublishAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the Pub/Sub client. */
        @get:Internal
        val service: Property<TopicAdminClientBuildService>

        /** GCP project ID containing the topic. */
        val projectId: Property<String>

        /** The topic ID (short name, not the full resource name). */
        val topicId: Property<String>

        /** The message data as a UTF-8 string. */
        val data: Property<String>

        /** Optional message attributes. */
        val attributes: MapProperty<String, String>

        /** Optional ordering key for ordered delivery. */
        val orderingKey: Property<String>
    }

    override fun execute() {
        val topicName = TopicName.of(parameters.projectId.get(), parameters.topicId.get())
        val message = PubsubMessage.newBuilder().apply {
            data = ByteString.copyFromUtf8(parameters.data.get())
            putAllAttributes(parameters.attributes.getOrElse(emptyMap()))
            if (parameters.orderingKey.isPresent) {
                orderingKey = parameters.orderingKey.get()
            }
        }.build()

        parameters.service.get().getClient().publish(topicName, listOf(message))
    }
}
