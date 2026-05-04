package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import aws.sdk.kotlin.services.sts.model.DecodeAuthorizationMessageRequest
import aws.sdk.kotlin.services.sts.model.StsException
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation that decodes an STS-encoded authorization failure message into a JSON document
 * describing the policy evaluation that produced the failure.
 *
 * Useful for surfacing IAM denial details in build logs. Returns `null` and logs a warning when the call throws
 * [StsException] (typically because the message has expired or the caller lacks `sts:DecodeAuthorizationMessage`).
 */
abstract class DecodeAuthorizationMessageValueSource : ValueSource<String, DecodeAuthorizationMessageValueSource.Parameters> {
    interface Parameters : ValueSourceParameters {
        /** The shared build service managing STS clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of an [StsClientInfo]. */
        val clientName: Property<String>

        /** The encoded authorization message to decode. */
        val encodedMessage: Property<String>
    }

    private val client: Provider<StsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): String? {
        val request = DecodeAuthorizationMessageRequest {
            encodedMessage = parameters.encodedMessage.get()
        }

        return try {
            runBlocking {
                client.get().decodeAuthorizationMessage(request).decodedMessage
            }
        } catch (e: StsException) {
            logger.warn("Unable to decode STS authorization message", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(DecodeAuthorizationMessageValueSource::class.java)
    }
}
