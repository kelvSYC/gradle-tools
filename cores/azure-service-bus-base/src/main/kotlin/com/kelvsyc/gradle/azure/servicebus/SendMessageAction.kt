package com.kelvsyc.gradle.azure.servicebus

import com.azure.messaging.servicebus.ServiceBusMessage
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation sending a single message to an Azure Service Bus queue or topic.
 *
 * The [Parameters.service] must reference a [ServiceBusSenderClientBuildService] configured
 * with either [queueName][ServiceBusSenderClientBuildService.Params.queueName] or
 * [topicName][ServiceBusSenderClientBuildService.Params.topicName].
 *
 * For session-ordered delivery, set [Parameters.sessionId]. For partitioned namespaces, set
 * [Parameters.partitionKey].
 */
abstract class SendMessageAction : WorkAction<SendMessageAction.Parameters> {
    /**
     * Parameters for [SendMessageAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the Service Bus sender client. */
        @get:Internal
        val service: Property<ServiceBusSenderClientBuildService>

        /** The message body as a UTF-8 string. */
        val body: Property<String>

        /** Optional message subject (analogous to SNS subject). */
        val subject: Property<String>

        /** Optional message identifier. */
        val messageId: Property<String>

        /** Optional session ID for session-ordered delivery. */
        val sessionId: Property<String>

        /** Optional partition key for partitioned namespaces. */
        val partitionKey: Property<String>

        /** Optional user-defined application properties. */
        val applicationProperties: MapProperty<String, String>
    }

    override fun execute() {
        val message = ServiceBusMessage(parameters.body.get()).apply {
            parameters.subject.orNull?.let { setSubject(it) }
            parameters.messageId.orNull?.let { setMessageId(it) }
            parameters.sessionId.orNull?.let { setSessionId(it) }
            parameters.partitionKey.orNull?.let { setPartitionKey(it) }
            applicationProperties.putAll(parameters.applicationProperties.getOrElse(emptyMap()))
        }
        parameters.service.get().getClient().sendMessage(message)
    }
}
