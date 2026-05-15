package com.kelvsyc.gradle.aws.java.sts

import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
import software.amazon.awssdk.services.sts.model.DecodeAuthorizationMessageRequest
import software.amazon.awssdk.services.sts.model.StsException

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
        val request = DecodeAuthorizationMessageRequest.builder()
            .encodedMessage(parameters.encodedMessage.get())
            .build()
        val client = parameters.service.get().getClient()

        return try {
            client.decodeAuthorizationMessage(request).decodedMessage()
        } catch (e: StsException) {
            logger.warn(e) { "Unable to decode STS authorization message" }
            null
        }
    }

    companion object {
        val logger by GradleLoggerDelegate
    }
}
