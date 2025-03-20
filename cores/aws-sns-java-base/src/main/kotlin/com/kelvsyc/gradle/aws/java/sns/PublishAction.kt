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
 * Only standard (non-FIFO) topics are supported, as this action does not expose message group or deduplication IDs.
 */
abstract class PublishAction : WorkAction<PublishAction.Parameters> {
    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val topicArn: Property<String>
        val subject: Property<String>
        val message: Property<String>
    }

    private val client: Provider<SnsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = PublishRequest.builder().apply {
            topicArn(parameters.topicArn.get())
            if (parameters.subject.isPresent) {
                subject(parameters.subject.get())
            }
            message(parameters.message.get())
        }.build()

        client.get().publish(request)
    }
}
