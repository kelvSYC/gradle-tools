package com.kelvsyc.gradle.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.SnsClient
import aws.sdk.kotlin.services.sns.model.PublishRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

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
        @get:Internal
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
        val request = PublishRequest {
            topicArn = parameters.topicArn.get()
            subject = parameters.subject.orNull
            message = parameters.message.get()
            messageGroupId = parameters.messageGroupId.orNull
            messageDeduplicationId = parameters.messageDeduplicationId.orNull
        }

        runBlocking {
            client.get().publish(request)
        }
    }
}
