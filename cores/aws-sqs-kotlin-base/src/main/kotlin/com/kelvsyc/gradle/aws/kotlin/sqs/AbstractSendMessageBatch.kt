package com.kelvsyc.gradle.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.model.MessageAttributeValue
import aws.sdk.kotlin.services.sqs.model.SendMessageBatchRequest
import aws.sdk.kotlin.services.sqs.model.SendMessageBatchRequestEntry
import kotlinx.coroutines.runBlocking
import org.gradle.api.Action
import org.gradle.api.DefaultTask
import org.gradle.api.GradleException
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
    private val objects: ObjectFactory
) : DefaultTask() {
    /**
     * The [SqsClient] used to send messages.
     */
    @get:Internal
    abstract val client: Property<SqsClient>

    /**
     * URL of the target SQS queue.
     */
    @get:Input
    abstract val queueUrl: Property<String>

    /**
     * Information about a single batch entry.
     */
    interface Entry {
        /**
         * Body of the message.
         */
        @get:Input
        val messageBody: Property<String>

        /**
         * Optional message attributes.
         */
        @get:Input
        @get:Optional
        val attributes: MapProperty<String, MessageAttributeValue>

        /**
         * Message group id; required for FIFO queues.
         */
        @get:Input
        @get:Optional
        val messageGroupId: Property<String>

        /**
         * Message deduplication id; used by FIFO queues without content-based deduplication.
         */
        @get:Input
        @get:Optional
        val messageDeduplicationId: Property<String>
    }

    /**
     * Entries to be sent. Users normally add to this collection through [registerEntry].
     */
    @get:Nested
    abstract val entries: MapProperty<String, Entry>

    /**
     * Registers a new entry. The supplied [name] is used as the batch entry id.
     */
    fun registerEntry(name: String, action: Action<in Entry>) {
        val entry = objects.newInstance<Entry>().also { action.execute(it) }
        entries.put(name, entry)
    }

    private fun toRequestEntry(name: String, entry: Entry): SendMessageBatchRequestEntry =
        SendMessageBatchRequestEntry {
            id = name
            messageBody = entry.messageBody.get()
            messageAttributes = entry.attributes.orNull
            messageGroupId = entry.messageGroupId.orNull
            messageDeduplicationId = entry.messageDeduplicationId.orNull
        }

    @TaskAction
    fun run() {
        val sqs = client.get()
        val url = queueUrl.get()
        val requestEntries = entries.get().map { (name, entry) -> toRequestEntry(name, entry) }

        val failed = mutableListOf<String>()
        runBlocking {
            requestEntries.chunked(SQS_BATCH_SIZE).forEach { chunk ->
                val request = SendMessageBatchRequest {
                    queueUrl = url
                    this.entries = chunk
                }
                val response = sqs.sendMessageBatch(request)
                response.failed.orEmpty().forEach { it.id?.let(failed::add) }
            }
        }

        if (failed.isNotEmpty()) {
            throw GradleException("SQS batch send failed for entries: ${failed.joinToString()}")
        }
    }

    private companion object {
        private const val SQS_BATCH_SIZE = 10
    }
}
