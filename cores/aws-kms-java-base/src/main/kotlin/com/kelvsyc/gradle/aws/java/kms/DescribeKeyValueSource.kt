package com.kelvsyc.gradle.aws.java.kms

import com.kelvsyc.gradle.clients.ClientsBaseService
import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.DescribeKeyRequest
import software.amazon.awssdk.services.kms.model.KmsException

/**
 * [ValueSource] implementation that retrieves the ARN of a KMS key from its ID, ARN, or alias name.
 *
 * Returns the canonical key ARN. Returns `null` and logs a warning when the call throws [KmsException] (e.g.
 * the key was not found or the caller lacks `kms:DescribeKey` permission).
 */
abstract class DescribeKeyValueSource : ValueSource<String, DescribeKeyValueSource.Parameters> {
    companion object {
        val logger by GradleLoggerDelegate
    }

    interface Parameters : ValueSourceParameters {
        /** The shared build service managing KMS clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of a [KmsClientInfo]. */
        val clientName: Property<String>

        /** The key ID, ARN, or alias name (e.g. `alias/my-key`) to describe. */
        val keyId: Property<String>
    }

    private val client: Provider<KmsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun obtain(): String? {
        val request = DescribeKeyRequest.builder().apply {
            keyId(parameters.keyId.get())
        }.build()

        return try {
            val response = client.get().describeKey(request)
            response.keyMetadata()?.arn()
        } catch (e: KmsException) {
            logger.warn(e) { "Unable to describe KMS key '${parameters.keyId.get()}'" }
            null
        }
    }
}
