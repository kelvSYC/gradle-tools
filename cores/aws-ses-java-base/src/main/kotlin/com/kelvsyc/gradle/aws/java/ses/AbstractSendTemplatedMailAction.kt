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
import org.gradle.api.tasks.Internal

/**
 * Base [WorkAction] for sending a templated email via SES.
 *
 * Subclasses may extend [Parameters] to add additional configuration.
 */
abstract class AbstractSendTemplatedMailAction<P : AbstractSendTemplatedMailAction.Parameters> : WorkAction<P> {
    interface Parameters : WorkParameters {
        /** The shared build service managing SES clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [SesClientInfo]. */
        val clientName: Property<String>

        /** The sender (From) email address. */
        val sender: Property<String>

        /** To addresses. */
        val recipients: ListProperty<String>

        /** CC addresses. */
        val ccAddresses: ListProperty<String>

        /** BCC addresses. */
        val bccAddresses: ListProperty<String>

        /** SES template name. */
        val templateName: Property<String>

        /**
         * Replacement values to be applied to the template.
         *
         * This value must be a JSON object, expressed as a string.
         */
        val templateJson: Property<String>
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
