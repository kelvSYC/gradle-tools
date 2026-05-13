package com.kelvsyc.gradle.aws.java.ses

import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.ses.model.RawMessage
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest

/**
 * Base [WorkAction] for sending a raw MIME email via SES.
 *
 * Subclasses may extend [Parameters] to add additional configuration.
 */
abstract class AbstractSendRawMailAction<P : AbstractSendRawMailAction.Parameters> : WorkAction<P> {
    interface Parameters : WorkParameters {
        /** The build service managing the SES client. */
        @get:Internal
        val service: Property<SesClientBuildService>

        /** The sender (From) email address. */
        val sender: Property<String>

        /** Raw MIME message bytes. */
        val message: Property<ByteArray>
    }

    private val messageInternal = parameters.message.map {
        RawMessage.builder().apply {
            data(SdkBytes.fromByteArray(it))
        }.build()
    }

    override fun execute() {
        val request = SendRawEmailRequest.builder().apply {
            source(parameters.sender.get())
            rawMessage(messageInternal.get())
        }.build()

        parameters.service.get().getClient().sendRawEmail(request)
    }
}
