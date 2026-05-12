package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.model.DescribeKeyRequest
import aws.sdk.kotlin.services.kms.model.KmsException
import kotlinx.coroutines.runBlocking
import org.gradle.api.logging.Logging
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters

/**
 * [ValueSource] implementation that retrieves the ARN of a KMS key from its ID, ARN, or alias name.
 *
 * Returns the canonical key ARN. Returns `null` and logs a warning when the call throws [KmsException] (e.g.
 * the key was not found or the caller lacks `kms:DescribeKey` permission).
 */
abstract class DescribeKeyValueSource : ValueSource<String, DescribeKeyValueSource.Parameters> {
    /**
     * Parameters for [DescribeKeyValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        val service: Property<KmsClientBuildService>

        /** The key ID, ARN, or alias name (e.g. `alias/my-key`) to describe. */
        val keyId: Property<String>
    }

    override fun obtain(): String? {
        val request = DescribeKeyRequest {
            keyId = parameters.keyId.get()
        }

        return try {
            runBlocking {
                parameters.service.get().getClient().describeKey(request).keyMetadata?.arn
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
