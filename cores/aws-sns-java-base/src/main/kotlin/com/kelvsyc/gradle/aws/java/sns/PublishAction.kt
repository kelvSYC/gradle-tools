package com.kelvsyc.gradle.aws.java.sns

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.sns.SnsClient
import software.amazon.awssdk.services.sns.model.PublishRequest

/**
 * [WorkAction] implementation publishing a message to an SNS topic.
 *
 * Only simple messages, to be sent across all transport protocols, are supported. JSON messages are not supported.
 *
 * For FIFO topics, set [Parameters.messageGroupId] (required) and optionally
 * [Parameters.messageDeduplicationId] (required if content-based deduplication is not enabled on the topic).
 */
abstract class PublishAction : WorkAction<PublishAction.Parameters> {
    /**
     * Parameters for [PublishAction].
     */
    interface Parameters : WorkParameters {
        /**
         * The shared [ClientsBaseService] holding the registered SNS client.
         */
        val service: Property<ClientsBaseService>

        /**
         * Registered name of an [SnsClientInfo].
         */
        val clientName: Property<String>

        /**
         * ARN of the target SNS topic.
         */
        val topicArn: Property<String>

        /**
         * Optional subject for the message (used by SNS email transport).
         */
        val subject: Property<String>

        /**
         * Body of the message.
         */
        val message: Property<String>

        /**
         * Message group id; required for FIFO topics, must be unset for standard topics.
         */
        val messageGroupId: Property<String>

        /**
         * Message deduplication id; used by FIFO topics without content-based deduplication.
         */
        val messageDeduplicationId: Property<String>
    }

    private val client: Provider<SnsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = PublishRequest.builder().apply {
            topicArn(parameters.topicArn.get())
            if (parameters.subject.isPresent) {
                subject(parameters.subject.get())
            }
            message(parameters.message.get())
            parameters.messageGroupId.orNull?.let { messageGroupId(it) }
            parameters.messageDeduplicationId.orNull?.let { messageDeduplicationId(it) }
        }.build()

        client.get().publish(request)
    }
}
