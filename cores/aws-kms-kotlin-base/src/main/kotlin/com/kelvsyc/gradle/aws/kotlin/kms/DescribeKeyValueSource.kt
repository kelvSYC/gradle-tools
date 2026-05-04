package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import aws.sdk.kotlin.services.kms.model.DescribeKeyRequest
import aws.sdk.kotlin.services.kms.model.KmsException
import com.kelvsyc.gradle.clients.ClientsBaseService
import kotlinx.coroutines.runBlocking
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation that retrieves the ARN of a KMS key from its ID, ARN, or alias name.
 *
 * Returns the canonical key ARN. Returns `null` and logs a warning when the call throws [KmsException] (e.g.
 * the key was not found or the caller lacks `kms:DescribeKey` permission).
 */
abstract class DescribeKeyValueSource : ValueSource<String, DescribeKeyValueSource.Parameters> {
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
        val request = DescribeKeyRequest {
            keyId = parameters.keyId.get()
        }

        return try {
            runBlocking {
                client.get().describeKey(request).keyMetadata?.arn
            }
        } catch (e: KmsException) {
            logger.warn("Unable to describe KMS key '${parameters.keyId.get()}'", e)
            null
        }
    }

    private companion object {
        private val logger = Logging.getLogger(DescribeKeyValueSource::class.java)
    }
}
