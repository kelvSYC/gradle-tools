package com.kelvsyc.gradle.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.model.MessageAttributeValue
import aws.sdk.kotlin.services.sqs.model.SendMessageRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Task that sends a single message to an SQS queue.
 *
 * For FIFO queues, set [messageGroupId] (required) and optionally [messageDeduplicationId]
 * (required if content-based deduplication is not enabled on the queue).
 */
@UntrackedTask(because = "Communicates with AWS SQS; no local output")
abstract class SendMessage : DefaultTask() {

    /** The shared build service managing the SQS client. */
    @get:Internal
    abstract val service: Property<SqsClientBuildService>

    /** URL of the target SQS queue. */
    @get:Input
    abstract val queueUrl: Property<String>

    /** Body of the message. */
    @get:Input
    abstract val messageBody: Property<String>

    /** Optional message attributes. */
    @get:Input
    abstract val attributes: MapProperty<String, MessageAttributeValue>

    /** Message group id; required for FIFO queues, must be unset for standard queues. */
    @get:Input
    @get:Optional
    abstract val messageGroupId: Property<String>

    /** Message deduplication id; used by FIFO queues without content-based deduplication. */
    @get:Input
    @get:Optional
    abstract val messageDeduplicationId: Property<String>

    @TaskAction
    fun execute() {
        val request = SendMessageRequest {
            queueUrl = this@SendMessage.queueUrl.get()
            messageBody = this@SendMessage.messageBody.get()
            messageAttributes = this@SendMessage.attributes.get()
            messageGroupId = this@SendMessage.messageGroupId.orNull
            messageDeduplicationId = this@SendMessage.messageDeduplicationId.orNull
        }

        runBlocking {
            service.get().getClient().sendMessage(request)
        }
    }
}
