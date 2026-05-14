package com.kelvsyc.gradle.google.cloud.kms

import com.google.api.gax.rpc.ApiException
import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that retrieves the canonical name of a Cloud KMS crypto key.
 *
 * Returns `null` and logs a warning when the call throws [ApiException] (e.g. the key was not
 * found or the caller lacks `cloudkms.cryptoKeys.get` permission).
 */
abstract class GetCryptoKeyValueSource : ValueSource<String, GetCryptoKeyValueSource.Parameters> {
    companion object {
        val logger by GradleLoggerDelegate
    }

    /**
     * Parameters for [GetCryptoKeyValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /**
         * Fully-qualified crypto key resource name, e.g.
         * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}`.
         */
        val cryptoKeyName: Property<String>
    }

    override fun obtain(): String? {
        return try {
            parameters.service.get().getClient().getCryptoKey(parameters.cryptoKeyName.get()).name
        } catch (e: ApiException) {
            logger.warn(e) { "Unable to get Cloud KMS crypto key '${parameters.cryptoKeyName.get()}'" }
            null
        }
    }
}
