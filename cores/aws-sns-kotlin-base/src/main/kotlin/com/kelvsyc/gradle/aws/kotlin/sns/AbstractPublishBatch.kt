package com.kelvsyc.gradle.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.MessageAttributeValue
import aws.sdk.kotlin.services.sns.model.PublishBatchRequest
import aws.sdk.kotlin.services.sns.model.PublishBatchRequestEntry
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
    private val objects: ObjectFactory
) : DefaultTask() {
    /**
     * The [SnsClient] used to publish messages.
     */
    @get:Internal
    abstract val client: Property<SnsClient>

    /**
     * ARN of the target SNS topic.
     */
    @get:Input
    abstract val topicArn: Property<String>

    /**
     * Information about a single batch entry.
     */
    interface Entry {
        /**
         * Body of the message.
         */
        @get:Input
        val message: Property<String>

        /**
         * Optional subject (used by SNS email transport).
         */
        @get:Input
        @get:Optional
        val subject: Property<String>

        /**
         * Optional message attributes.
         */
        @get:Input
        @get:Optional
        val attributes: MapProperty<String, MessageAttributeValue>

        /**
         * Message group id; required for FIFO topics.
         */
        @get:Input
        @get:Optional
        val messageGroupId: Property<String>

        /**
         * Message deduplication id; used by FIFO topics without content-based deduplication.
         */
        @get:Input
        @get:Optional
        val messageDeduplicationId: Property<String>
    }

    /**
     * Entries to be published. Users normally add to this collection through [registerEntry].
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

    private fun toRequestEntry(name: String, entry: Entry): PublishBatchRequestEntry =
        PublishBatchRequestEntry {
            id = name
            message = entry.message.get()
            subject = entry.subject.orNull
            messageAttributes = entry.attributes.orNull
            messageGroupId = entry.messageGroupId.orNull
            messageDeduplicationId = entry.messageDeduplicationId.orNull
        }

    @TaskAction
    fun run() {
        val sns = client.get()
        val arn = topicArn.get()
        val requestEntries = entries.get().map { (name, entry) -> toRequestEntry(name, entry) }

        val failed = mutableListOf<String>()
        runBlocking {
            requestEntries.chunked(SNS_BATCH_SIZE).forEach { chunk ->
                val request = PublishBatchRequest {
                    topicArn = arn
                    publishBatchRequestEntries = chunk
                }
                val response = sns.publishBatch(request)
                response.failed.orEmpty().forEach { it.id?.let(failed::add) }
            }
        }

        if (failed.isNotEmpty()) {
            throw GradleException("SNS batch publish failed for entries: ${failed.joinToString()}")
        }
    }

    private companion object {
        private const val SNS_BATCH_SIZE = 10
    }
}
