package com.kelvsyc.gradle.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.SqsClient
import aws.sdk.kotlin.services.sqs.model.MessageAttributeValue
import aws.sdk.kotlin.services.sqs.model.SendMessageRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.MapProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

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
        val request = SendMessageRequest {
            queueUrl = parameters.queueUrl.get()
            messageBody = parameters.messageBody.get()
            messageAttributes = parameters.attributes.get()
        }

        runBlocking {
            client.get().sendMessage(request)
        }
    }
}
