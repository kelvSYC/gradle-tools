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

abstract class AbstractSendRawMailAction<P : AbstractSendRawMailAction.Parameters> : WorkAction<P> {
    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val sender: Property<String>
        val message: Provider<ByteArray>
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
