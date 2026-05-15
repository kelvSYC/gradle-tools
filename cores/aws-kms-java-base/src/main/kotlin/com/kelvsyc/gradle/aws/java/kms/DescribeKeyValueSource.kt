package com.kelvsyc.gradle.aws.java.kms

import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal
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

    /**
     * Parameters for [DescribeKeyValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /** The key ID, ARN, or alias name (e.g. `alias/my-key`) to describe. */
        val keyId: Property<String>
    }

    override fun obtain(): String? {
        val request = DescribeKeyRequest.builder()
            .keyId(parameters.keyId.get())
            .build()

        return try {
            parameters.service.get().getClient().describeKey(request).keyMetadata()?.arn()
        } catch (e: KmsException) {
            logger.warn(e) { "Unable to describe KMS key '${parameters.keyId.get()}'" }
            null
        }
    }
}
