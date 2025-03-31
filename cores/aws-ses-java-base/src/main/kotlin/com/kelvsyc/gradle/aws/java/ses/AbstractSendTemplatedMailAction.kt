package com.kelvsyc.gradle.aws.java.ses

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.Destination
import software.amazon.awssdk.services.ses.model.SendTemplatedEmailRequest

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
        val destination = Destination.builder().apply {
            toAddresses(parameters.recipients.getOrElse(emptyList()))
            ccAddresses(parameters.ccAddresses.getOrElse(emptyList()))
            bccAddresses(parameters.bccAddresses.getOrElse(emptyList()))
        }.build()

        val request = SendTemplatedEmailRequest.builder().apply {
            source(parameters.sender.get())
            destination(destination)
            template(parameters.templateName.get())
            templateData(parameters.templateJson.get())
        }.build()

        client.get().sendTemplatedEmail(request)
    }
}
