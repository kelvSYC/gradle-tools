package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.model.Destination
import aws.sdk.kotlin.services.ses.model.SendTemplatedEmailRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.ListProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Base task for sending a templated email via SES.
 *
 * Subclasses may add additional properties with appropriate Gradle annotations.
 */
@UntrackedTask(because = "Communicates with AWS SES; no local output")
abstract class AbstractSendTemplatedMail : DefaultTask() {

    /** The shared build service managing the SES client. */
    @get:Internal
    abstract val service: Property<SesClientBuildService>

    /** The sender (From) email address. */
    @get:Input
    abstract val sender: Property<String>

    /** To addresses. */
    @get:Input
    abstract val recipients: ListProperty<String>

    /** CC addresses. */
    @get:Input
    @get:Optional
    abstract val ccAddresses: ListProperty<String>

    /** BCC addresses. */
    @get:Input
    @get:Optional
    abstract val bccAddresses: ListProperty<String>

    /** SES template name. */
    @get:Input
    abstract val templateName: Property<String>

    /** Replacement values to be applied to the template. This value must be a JSON object, expressed as a string. */
    @get:Input
    abstract val templateJson: Property<String>

    @TaskAction
    fun execute() = runBlocking {
        val destination = Destination {
            toAddresses = recipients.getOrElse(emptyList())
            ccAddresses = this@AbstractSendTemplatedMail.ccAddresses.getOrElse(emptyList())
            bccAddresses = this@AbstractSendTemplatedMail.bccAddresses.getOrElse(emptyList())
        }

        val request = SendTemplatedEmailRequest {
            source = sender.get()
            this.destination = destination
            template = templateName.get()
            templateData = templateJson.get()
        }

        service.get().getClient().sendTemplatedEmail(request)
    }
}
