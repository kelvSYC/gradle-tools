package com.kelvsyc.gradle.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.model.MessageAttributeValue
import aws.sdk.kotlin.services.sqs.model.SendMessageRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

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
        /**
         * The shared [SqsClientBuildService] managing the SQS client.
         */
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
        val request = SendMessageRequest {
            queueUrl = parameters.queueUrl.get()
            messageBody = parameters.messageBody.get()
            messageAttributes = parameters.attributes.get()
            messageGroupId = parameters.messageGroupId.orNull
            messageDeduplicationId = parameters.messageDeduplicationId.orNull
        }

        runBlocking {
            parameters.service.get().getClient().sendMessage(request)
        }
    }
}
