package com.kelvsyc.gradle.azure.servicebus

import com.azure.messaging.servicebus.ServiceBusMessage
import com.azure.messaging.servicebus.ServiceBusSenderClient
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
 * Sends an arbitrary number of messages to an Azure Service Bus queue or topic.
 *
 * Messages are submitted using the Service Bus atomic batch API. The SDK enforces a size limit
 * (256 KB for Standard tier, 1 MB for Premium tier) per batch via [tryAddMessage][com.azure.messaging.servicebus.ServiceBusMessageBatch.tryAddMessage];
 * when a batch fills up it is flushed automatically and a new batch is started. A single message
 * that exceeds the limit on its own will cause the task to fail.
 *
 * Specify entries using [registerEntry]. Each entry's name uniquely identifies it within this task.
 * For session-ordered delivery, set [Entry.sessionId]. For partitioned namespaces, set
 * [Entry.partitionKey].
 */
@DisableCachingByDefault(because = "Sending to an external service is not cacheable")
abstract class AbstractSendMessageBatch @Inject constructor(
    private val objects: ObjectFactory,
) : DefaultTask() {

    /**
     * The [ServiceBusSenderClient] used to send messages.
     */
    @get:Internal
    abstract val client: Property<ServiceBusSenderClient>

    /**
     * A single message entry in the batch.
     */
    interface Entry {
        /** The message body as a UTF-8 string. */
        @get:Input
        val body: Property<String>

        /** Optional message subject (analogous to SNS subject). */
        @get:Input
        @get:Optional
        val subject: Property<String>

        /** Optional message identifier. */
        @get:Input
        @get:Optional
        val messageId: Property<String>

        /** Optional session ID for session-ordered delivery. */
        @get:Input
        @get:Optional
        val sessionId: Property<String>

        /** Optional partition key for partitioned namespaces. */
        @get:Input
        @get:Optional
        val partitionKey: Property<String>

        /** Optional user-defined application properties. */
        @get:Input
        @get:Optional
        val applicationProperties: MapProperty<String, String>
    }

    /** Entries to be sent. Users normally add to this collection through [registerEntry]. */
    @get:Nested
    abstract val entries: MapProperty<String, Entry>

    /**
     * Registers a new message entry. The supplied [name] identifies the entry within this task.
     */
    fun registerEntry(name: String, action: Action<in Entry>) {
        val entry = objects.newInstance<Entry>().also { action.execute(it) }
        entries.put(name, entry)
    }

    private fun toMessage(entry: Entry): ServiceBusMessage =
        ServiceBusMessage(entry.body.get()).apply {
            entry.subject.orNull?.let { setSubject(it) }
            entry.messageId.orNull?.let { setMessageId(it) }
            entry.sessionId.orNull?.let { setSessionId(it) }
            entry.partitionKey.orNull?.let { setPartitionKey(it) }
            applicationProperties.putAll(entry.applicationProperties.getOrElse(emptyMap()))
        }

    @TaskAction
    fun run() {
        val sender = client.get()
        val allMessages = entries.get().values.map { toMessage(it) }
        var batch = sender.createMessageBatch()
        for (message in allMessages) {
            if (!batch.tryAddMessage(message)) {
                sender.sendMessages(batch)
                batch = sender.createMessageBatch()
                check(batch.tryAddMessage(message)) { "Message exceeds maximum batch size" }
            }
        }
        if (batch.count > 0) sender.sendMessages(batch)
    }
}
