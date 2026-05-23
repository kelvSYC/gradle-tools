package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.model.Body
import aws.sdk.kotlin.services.ses.model.Content
import aws.sdk.kotlin.services.ses.model.Destination
import aws.sdk.kotlin.services.ses.model.Message
import aws.sdk.kotlin.services.ses.model.SendEmailRequest
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
 * Task that sends a simple email via SES.
 *
 * At least one of [htmlMessage] or [textMessage] should be provided.
 */
@UntrackedTask(because = "Communicates with AWS SES; no local output")
abstract class SendMail : DefaultTask() {

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

    /** Email subject line. */
    @get:Input
    abstract val subject: Property<String>

    /** HTML body content. */
    @get:Input
    @get:Optional
    abstract val htmlMessage: Property<String>

    /** Plain-text body content. */
    @get:Input
    @get:Optional
    abstract val textMessage: Property<String>

    @TaskAction
    fun execute() = runBlocking {
        val subjectContent = Content {
            data = subject.get()
        }
        val htmlMessageContent = if (htmlMessage.isPresent) {
            Content {
                data = htmlMessage.get()
            }
        } else {
            null
        }
        val textMessageContent = if (textMessage.isPresent) {
            Content {
                data = textMessage.get()
            }
        } else {
            null
        }

        val destination = Destination {
            toAddresses = recipients.getOrElse(emptyList())
            ccAddresses = this@SendMail.ccAddresses.getOrElse(emptyList())
            bccAddresses = this@SendMail.bccAddresses.getOrElse(emptyList())
        }
        val messageBody = Body {
            html = htmlMessageContent
            text = textMessageContent
        }
        val message = Message {
            subject = subjectContent
            body = messageBody
        }

        val request = SendEmailRequest {
            source = sender.get()
            this.destination = destination
            this.message = message
        }

        service.get().getClient().sendEmail(request)
    }
}
