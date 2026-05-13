package com.kelvsyc.gradle.aws.java.sqs

import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest

/**
 * A [WorkAction] that sends a single message to an SQS queue.
 *
 * For FIFO queues, set [Parameters.messageGroupId] (required) and optionally [Parameters.messageDeduplicationId]
 * (required if content-based deduplication is not enabled on the queue).
 */
abstract class SendMessageAction : WorkAction<SendMessageAction.Parameters> {
    /**
     * Parameters for [SendMessageAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the SQS client. */
        @get:Internal
        val service: Property<SqsClientBuildService>

        /**
         * URL of the target SQS queue.
         */
        val queueUrl: Property<String>

        /**
         * Body of the message.
         */
        val messageBody: Property<String>

        /**
         * Optional message attributes.
         */
        val attributes: MapProperty<String, MessageAttributeValue>

        /**
         * Message group id; required for FIFO queues, must be unset for standard queues.
         */
        val messageGroupId: Property<String>

        /**
         * Message deduplication id; used by FIFO queues without content-based deduplication.
         */
        val messageDeduplicationId: Property<String>
    }

    override fun execute() {
        val request = SendMessageRequest.builder().apply {
            queueUrl(parameters.queueUrl.get())
            messageBody(parameters.messageBody.get())
            if (parameters.attributes.isPresent) {
                messageAttributes(parameters.attributes.get())
            }
            parameters.messageGroupId.orNull?.let { messageGroupId(it) }
            parameters.messageDeduplicationId.orNull?.let { messageDeduplicationId(it) }
        }.build()

        parameters.service.get().getClient().sendMessage(request)
    }
}
