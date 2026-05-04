package com.kelvsyc.gradle.google.cloud.pubsub

import com.google.cloud.pubsub.v1.TopicAdminClient
import com.google.protobuf.ByteString
import com.google.pubsub.v1.PubsubMessage
import com.google.pubsub.v1.TopicName
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Nested
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import javax.inject.Inject

/**
 * Publishes an arbitrary number of messages to a Google Cloud Pub/Sub topic.
 *
 * The Pub/Sub `Publish` API accepts up to 1000 messages per request; this task internally chunks entries
 * to that limit, so callers may register any number of entries.
 *
 * Specify entries using [registerEntry]. For topics with message ordering, set [Entry.orderingKey].
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class AbstractPublishBatch @Inject constructor(
    private val objects: ObjectFactory,
) : DefaultTask() {
    /**
     * The [TopicAdminClient] used to publish messages.
     */
    @get:Internal
    abstract val client: Property<TopicAdminClient>

    /** GCP project ID containing the topic. */
    @get:Input
    abstract val projectId: Property<String>

    /** The topic ID (short name, not the full resource name). */
    @get:Input
    abstract val topicId: Property<String>

    /**
     * A single message entry in the batch.
     */
    interface Entry {
        /** The message data as a UTF-8 string. */
        @get:Input
        val data: Property<String>

        /** Optional message attributes. */
        @get:Input
        @get:Optional
        val attributes: MapProperty<String, String>

        /** Optional ordering key for ordered delivery. */
        @get:Input
        @get:Optional
        val orderingKey: Property<String>
    }

    /** Entries to be published. Users normally add to this collection through [registerEntry]. */
    @get:Nested
    abstract val entries: MapProperty<String, Entry>

    /**
     * Registers a new message entry. The supplied [name] identifies the entry within this task.
     */
    fun registerEntry(name: String, action: Action<in Entry>) {
        val entry = objects.newInstance<Entry>().also { action.execute(it) }
        entries.put(name, entry)
    }

    private fun toMessage(entry: Entry): PubsubMessage =
        PubsubMessage.newBuilder().apply {
            data = ByteString.copyFromUtf8(entry.data.get())
            putAllAttributes(entry.attributes.getOrElse(emptyMap()))
            if (entry.orderingKey.isPresent) {
                orderingKey = entry.orderingKey.get()
            }
        }.build()

    @TaskAction
    fun run() {
        val topicAdminClient = client.get()
        val topicName = TopicName.of(projectId.get(), topicId.get())
        val messages = entries.get().values.map { toMessage(it) }

        messages.chunked(PUBSUB_BATCH_SIZE).forEach { chunk ->
            topicAdminClient.publish(topicName, chunk)
        }
    }

    private companion object {
        private const val PUBSUB_BATCH_SIZE = 1000
    }
}
