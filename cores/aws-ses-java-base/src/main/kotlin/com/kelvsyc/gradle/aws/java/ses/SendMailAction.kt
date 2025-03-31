package com.kelvsyc.gradle.aws.java.ses

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.Body
import software.amazon.awssdk.services.ses.model.Content
import software.amazon.awssdk.services.ses.model.Destination
import software.amazon.awssdk.services.ses.model.Message
import software.amazon.awssdk.services.ses.model.SendEmailRequest

abstract class SendMailAction : WorkAction<SendMailAction.Parameters> {
    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val sender: Property<String>
        val recipients: ListProperty<String>
        val ccAddresses: ListProperty<String>
        val bccAddresses: ListProperty<String>
        val subject: Property<String>
        val htmlMessage: Property<String>
        val textMessage: Property<String>
    }

    private val client: Provider<SesClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    private val subjectContent = parameters.subject.map {
        Content.builder().apply {
            data(it)
        }.build()
    }
    private val htmlMessageContent = parameters.htmlMessage.map {
        Content.builder().apply {
            data(it)
        }.build()
    }
    private val textMessageContent = parameters.textMessage.map {
        Content.builder().apply {
            data(it)
        }.build()
    }

    override fun execute() {
        val destination = Destination.builder().apply {
            toAddresses(parameters.recipients.getOrElse(emptyList()))
            ccAddresses(parameters.ccAddresses.getOrElse(emptyList()))
            bccAddresses(parameters.bccAddresses.getOrElse(emptyList()))
        }.build()
        val messageBody = Body.builder().apply {
            if (htmlMessageContent.isPresent) {
                html(htmlMessageContent.get())
            }
            if (textMessageContent.isPresent) {
                text(textMessageContent.get())
            }
        }.build()
        val message = Message.builder().apply {
            subject(subjectContent.get())
            body(messageBody)
        }.build()

        val request = SendEmailRequest.builder().apply {
            source(parameters.sender.get())
            destination(destination)
            message(message)
        }.build()

        client.get().sendEmail(request)
    }
}
