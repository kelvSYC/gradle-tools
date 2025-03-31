package com.kelvsyc.gradle.aws.java.ses

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.ses.SesClient
import software.amazon.awssdk.services.ses.model.RawMessage
import software.amazon.awssdk.services.ses.model.SendRawEmailRequest

abstract class AbstractSendRawMailAction<P : AbstractSendRawMailAction.Parameters> : WorkAction<P> {
    interface Parameters : WorkParameters {
        val service: Property<ClientsBaseService>
        val clientName: Property<String>

        val sender: Property<String>
        val message: Provider<ByteArray>
    }

    private val client: Provider<SesClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

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

        client.get().sendRawEmail(request)
    }
}
