package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import aws.sdk.kotlin.services.ses.model.RawMessage
import aws.sdk.kotlin.services.ses.model.SendRawEmailRequest
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import org.gradle.api.tasks.Internal

/**
 * Base [WorkAction] for sending a raw MIME email via SES.
 *
 * Subclasses may extend [Parameters] to add additional configuration.
 */
abstract class AbstractSendRawMailAction<P : AbstractSendRawMailAction.Parameters> : WorkAction<P> {
    interface Parameters : WorkParameters {
        /** The shared build service managing SES clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of a [SesClientInfo]. */
        val clientName: Property<String>

        /** The sender (From) email address. */
        val sender: Property<String>

        /** Raw MIME message bytes. */
        val message: Property<ByteArray>
    }

    private val client: Provider<SesClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    private val messageInternal = parameters.message.map {
        RawMessage {
            data = it
        }
    }

    override fun execute() {
        val request = SendRawEmailRequest {
            source = parameters.sender.get()
            rawMessage = messageInternal.get()
        }

        runBlocking {
            client.get().sendRawEmail(request)
        }
    }
}
