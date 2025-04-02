package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.Body
import aws.sdk.kotlin.services.ses.model.Content
import aws.sdk.kotlin.services.ses.model.Destination
import aws.sdk.kotlin.services.ses.model.Message
import aws.sdk.kotlin.services.ses.model.SendEmailRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

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
        Content {
            data = it
        }
    }

    private val htmlMessageContent = parameters.htmlMessage.map {
        Content {
            data = it
        }
    }
    private val textMessageContent = parameters.textMessage.map {
        Content {
            data = it
        }
    }

    override fun execute() {
        val destination = Destination {
            toAddresses = parameters.recipients.getOrElse(emptyList())
            ccAddresses = parameters.ccAddresses.getOrElse(emptyList())
            bccAddresses = parameters.bccAddresses.getOrElse(emptyList())
        }
        val messageBody = Body {
            html = htmlMessageContent.orNull
            text = textMessageContent.orNull
        }
        val message = Message {
            subject = subjectContent.get()
            body = messageBody
        }

        val request = SendEmailRequest {
            source = parameters.sender.get()
            this.destination = destination
            this.message = message
        }

        runBlocking {
            client.get().sendEmail(request)
        }
    }
}
