package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.model.Destination
import aws.sdk.kotlin.services.ses.model.SendTemplatedEmailRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * Base [WorkAction] for sending a templated email via SES.
 *
 * Subclasses may extend [Parameters] to add additional configuration.
 */
abstract class AbstractSendTemplatedMailAction<P : AbstractSendTemplatedMailAction.Parameters> : WorkAction<P> {
    interface Parameters : WorkParameters {
        /** The shared build service managing the SES client. */
        @get:Internal
        val service: Property<SesClientBuildService>

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
            parameters.service.get().getClient().sendTemplatedEmail(request)
        }
    }
}
