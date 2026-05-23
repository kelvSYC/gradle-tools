package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.model.RawMessage
import aws.sdk.kotlin.services.ses.model.SendRawEmailRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.TaskAction
import org.gradle.api.tasks.UntrackedTask

/**
 * Base task for sending a raw MIME email via SES.
 *
 * Subclasses may add additional properties with appropriate Gradle annotations.
 */
@UntrackedTask(because = "Communicates with AWS SES; no local output")
abstract class AbstractSendRawMail : DefaultTask() {

    /** The shared build service managing the SES client. */
    @get:Internal
    abstract val service: Property<SesClientBuildService>

    /** The sender (From) email address. */
    @get:Input
    abstract val sender: Property<String>

    /** Raw MIME message bytes. */
    @get:Input
    abstract val message: Property<ByteArray>

    @TaskAction
    fun execute() = runBlocking {
        val messageInternal = RawMessage {
            data = message.get()
        }

        val request = SendRawEmailRequest {
            source = sender.get()
            rawMessage = messageInternal
        }

        service.get().getClient().sendRawEmail(request)
    }
}
