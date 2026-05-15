package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.model.DecodeAuthorizationMessageRequest
import aws.sdk.kotlin.services.sts.model.StsException
import kotlinx.coroutines.runBlocking
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that decodes an STS-encoded authorization failure message into a JSON document
 * describing the policy evaluation that produced the failure.
 *
 * Useful for surfacing IAM denial details in build logs. Returns `null` and logs a warning when the call throws
 * [StsException] (typically because the message has expired or the caller lacks `sts:DecodeAuthorizationMessage`).
 */
abstract class DecodeAuthorizationMessageValueSource :
    ValueSource<String, DecodeAuthorizationMessageValueSource.Parameters> {
    /**
     * Parameters for [DecodeAuthorizationMessageValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the STS client. */
        @get:Internal
        val service: Property<StsClientBuildService>

        /** The encoded authorization message to decode. */
        val encodedMessage: Property<String>
    }

    override fun obtain(): String? {
        val request = DecodeAuthorizationMessageRequest {
            encodedMessage = parameters.encodedMessage.get()
        }
        val client = parameters.service.get().getClient()

        return try {
            runBlocking {
                client.decodeAuthorizationMessage(request).decodedMessage
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
