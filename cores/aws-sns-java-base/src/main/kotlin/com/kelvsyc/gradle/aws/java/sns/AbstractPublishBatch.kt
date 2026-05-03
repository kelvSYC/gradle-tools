package com.kelvsyc.gradle.aws.java.sns

import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.Named
import org.gradle.api.model.ObjectFactory
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.ProviderFactory
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.kotlin.dsl.newInstance
import org.gradle.work.DisableCachingByDefault
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.MessageAttributeValue
import software.amazon.awssdk.services.sns.model.PublishBatchRequest
import software.amazon.awssdk.services.sns.model.PublishBatchRequestEntry
import javax.inject.Inject

/**
 * Publishes an arbitrary number of messages to an SNS topic. Entries are submitted using the SNS
 * `PublishBatch` API; the task internally chunks entries into the maximum batch size supported by SNS, so
 * callers may register any number of entries.
 *
 * Specify entries using [registerEntry]. Each entry's name is used as the SNS batch entry id and must be
 * unique within the task. For FIFO topics, set [Entry.messageGroupId] (required) and optionally
 * [Entry.messageDeduplicationId].
 *
 * The task fails if any batch call fails. Per-entry failures within an otherwise-successful batch call are
 * reported as a task failure listing the failed entry ids.
 */
@DisableCachingByDefault(because = "Publishing to an external service is not cacheable")
abstract class AbstractPublishBatch @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory
) : DefaultTask() {
    /**
     * The [SnsClient] used to publish messages.
     */
    @get:Internal
    abstract val client: Property<SnsClient>

    /**
     * ARN of the target SNS topic.
     */
    @get:Internal
    abstract val topicArn: Property<String>

    /**
     * Information about a single batch entry.
     */
    abstract class Entry @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        /**
         * Body of the message.
         */
        abstract val message: Property<String>

        /**
         * Optional subject (used by SNS email transport).
         */
        abstract val subject: Property<String>

        /**
         * Optional message attributes.
         */
        abstract val attributes: MapProperty<String, MessageAttributeValue>

        /**
         * Message group id; required for FIFO topics.
         */
        abstract val messageGroupId: Property<String>

        /**
         * Message deduplication id; used by FIFO topics without content-based deduplication.
         */
        abstract val messageDeduplicationId: Property<String>
    }

    /**
     * Entries to be published. Users normally add to this collection through [registerEntry].
     */
    @get:Internal
    abstract val entries: MapProperty<String, Entry>

    /**
     * Registers a new entry. The supplied [name] is used as the batch entry id.
     */
    fun registerEntry(name: String, configureAction: Action<in Entry>) {
        entries.put(name, providers.provider {
            objects.newInstance<Entry>(name).also { configureAction.execute(it) }
        })
    }

    private fun toRequestEntry(entry: Entry): PublishBatchRequestEntry =
        PublishBatchRequestEntry.builder().apply {
            id(entry.name)
            message(entry.message.get())
            entry.subject.orNull?.let { subject(it) }
            if (entry.attributes.isPresent) {
                messageAttributes(entry.attributes.get())
            }
            entry.messageGroupId.orNull?.let { messageGroupId(it) }
            entry.messageDeduplicationId.orNull?.let { messageDeduplicationId(it) }
        }.build()

    @TaskAction
    fun run() {
        val sns = client.get()
        val arn = topicArn.get()
        val requestEntries = entries.get().values.map(::toRequestEntry)

        val failed = mutableListOf<String>()
        requestEntries.chunked(SNS_BATCH_SIZE).forEach { chunk ->
            val request = PublishBatchRequest.builder().apply {
                topicArn(arn)
                publishBatchRequestEntries(chunk)
            }.build()
            val response = sns.publishBatch(request)
            response.failed().forEach { failed += it.id() }
        }

        if (failed.isNotEmpty()) {
            error("SNS batch publish failed for entries: ${failed.joinToString()}")
        }
    }

    private companion object {
        private const val SNS_BATCH_SIZE = 10
    }
}
