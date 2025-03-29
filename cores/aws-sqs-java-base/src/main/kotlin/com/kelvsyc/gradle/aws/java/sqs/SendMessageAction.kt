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

abstract class SendMessageAction : WorkAction<SendMessageAction.Parameters> {
    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName : Property<String>

        val queueUrl: Property<String>
        val messageBody: Property<String>
        val attributes: MapProperty<String, MessageAttributeValue>
    }

    private val client: Provider<SqsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val request = SendMessageRequest.builder().apply {
            queueUrl(parameters.queueUrl.get())
            messageBody(parameters.messageBody.get())
            if (parameters.attributes.isPresent) {
                messageAttributes(parameters.attributes.get())
            }
        }.build()

        client.get().sendMessage(request)
    }
}
