package com.kelvsyc.gradle.aws.java.sqs

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.sqs.SqsClient
import software.amazon.awssdk.services.sqs.model.MessageAttributeValue
import software.amazon.awssdk.services.sqs.model.SendMessageRequest
import org.gradle.api.tasks.Internal

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
         * The shared [ClientsBaseService] holding the registered SQS client.
         */
        @get:Internal
        val service: Property<ClientsBaseService>

        /**
         * Registered name of an [SqsClientInfo].
         */
        val clientName: Property<String>

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

    private val client: Provider<SqsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

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

        client.get().sendMessage(request)
    }
}
