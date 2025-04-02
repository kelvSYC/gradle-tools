package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.Destination
import aws.sdk.kotlin.services.ses.model.SendTemplatedEmailRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

abstract class AbstractSendTemplatedMailAction<P : AbstractSendTemplatedMailAction.Parameters> : WorkAction<P> {
    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val sender: Property<String>
        val recipients: ListProperty<String>
        val ccAddresses: ListProperty<String>
        val bccAddresses: ListProperty<String>
        val templateName: Property<String>

        /**
         * Replacement values to be applied to the template.
         *
         * This value must be a JSON object, expressed as a string.
         */
        val templateJson: Provider<String>
    }

    private val client: Provider<SesClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val destination = Destination {
            toAddresses = parameters.recipients.getOrElse(emptyList())
            ccAddresses = parameters.ccAddresses.getOrElse(emptyList())
            bccAddresses = parameters.bccAddresses.getOrElse(emptyList())
        }

        val request = SendTemplatedEmailRequest {
            source = parameters.sender.get()
            this.destination = destination
            template = parameters.templateName.get()
            templateData = parameters.templateJson.get()
        }

        runBlocking {
            client.get().sendTemplatedEmail(request)
        }
    }
}
