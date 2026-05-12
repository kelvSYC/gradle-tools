package com.kelvsyc.gradle.aws.java.sts

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.sts.StsClient
import software.amazon.awssdk.services.sts.model.DecodeAuthorizationMessageRequest
import software.amazon.awssdk.services.sts.model.StsException
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that decodes an STS-encoded authorization failure message into a JSON document
 * describing the policy evaluation that produced the failure.
 *
 * Useful for surfacing IAM denial details in build logs. Returns `null` and logs a warning when the call throws
 * [StsException] (typically because the message has expired or the caller lacks `sts:DecodeAuthorizationMessage`).
 */
abstract class DecodeAuthorizationMessageValueSource : ValueSource<String, DecodeAuthorizationMessageValueSource.Parameters> {
    companion object {
        val logger by GradleLoggerDelegate
    }

    interface Parameters : ValueSourceParameters {
        /** The shared build service managing STS clients. */
        @get:Internal
        val service: Property<ClientsBaseService>

        /** Registered name of an [StsClientInfo]. */
        val clientName: Property<String>

        /** The encoded authorization message to decode. */
        val encodedMessage: Property<String>
    }

    private val client: Provider<StsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): String? {
        val request = DecodeAuthorizationMessageRequest.builder().apply {
            encodedMessage(parameters.encodedMessage.get())
        }.build()

        return try {
            client.get().decodeAuthorizationMessage(request).decodedMessage()
        } catch (e: StsException) {
            logger.warn(e) { "Unable to decode STS authorization message" }
            null
        }
    }
}
