package com.kelvsyc.gradle.aws.java.sqs

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
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequest
import software.amazon.awssdk.services.sqs.model.SendMessageBatchRequestEntry
import javax.inject.Inject

/**
 * Sends an arbitrary number of messages to an SQS queue. Entries are submitted using the SQS batch
 * `SendMessageBatch` API; the task internally chunks entries into the maximum batch size supported by SQS,
 * so callers may register any number of entries.
 *
 * Specify entries using [registerEntry]. Each entry's name is used as the SQS batch entry id and must be
 * unique within the task. For FIFO queues, set [Entry.messageGroupId] (required) and optionally
 * [Entry.messageDeduplicationId].
 *
 * The task fails if any batch call fails. Per-entry failures within an otherwise-successful batch call are
 * reported as a task failure listing the failed entry ids.
 */
@DisableCachingByDefault(because = "Sending to an external service is not cacheable")
abstract class AbstractSendMessageBatch @Inject constructor(
    private val objects: ObjectFactory,
    private val providers: ProviderFactory
) : DefaultTask() {
    /**
     * The [SqsClient] used to send messages.
     */
    @get:Internal
    abstract val client: Property<SqsClient>

    /**
     * URL of the target SQS queue.
     */
    @get:Internal
    abstract val queueUrl: Property<String>

    /**
     * Information about a single batch entry.
     */
    abstract class Entry @Inject constructor(private val name: String) : Named {
        override fun getName() = name

        /**
         * Body of the message.
         */
        abstract val messageBody: Property<String>

        /**
         * Optional message attributes.
         */
        abstract val attributes: MapProperty<String, MessageAttributeValue>

        /**
         * Message group id; required for FIFO queues.
         */
        abstract val messageGroupId: Property<String>

        /**
         * Message deduplication id; used by FIFO queues without content-based deduplication.
         */
        abstract val messageDeduplicationId: Property<String>
    }

    /**
     * Entries to be sent. Users normally add to this collection through [registerEntry].
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

    private fun toRequestEntry(entry: Entry): SendMessageBatchRequestEntry =
        SendMessageBatchRequestEntry.builder().apply {
            id(entry.name)
            messageBody(entry.messageBody.get())
            if (entry.attributes.isPresent) {
                messageAttributes(entry.attributes.get())
            }
            entry.messageGroupId.orNull?.let { messageGroupId(it) }
            entry.messageDeduplicationId.orNull?.let { messageDeduplicationId(it) }
        }.build()

    @TaskAction
    fun run() {
        val sqs = client.get()
        val url = queueUrl.get()
        val requestEntries = entries.get().values.map(::toRequestEntry)

        val failed = mutableListOf<String>()
        requestEntries.chunked(SQS_BATCH_SIZE).forEach { chunk ->
            val request = SendMessageBatchRequest.builder().apply {
                queueUrl(url)
                entries(chunk)
            }.build()
            val response = sqs.sendMessageBatch(request)
            response.failed().forEach { failed += it.id() }
        }

        if (failed.isNotEmpty()) {
            error("SQS batch send failed for entries: ${failed.joinToString()}")
        }
    }

    private companion object {
        private const val SQS_BATCH_SIZE = 10
    }
}
