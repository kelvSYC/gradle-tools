package com.kelvsyc.gradle.google.cloud.kms

import com.google.api.gax.rpc.ApiException
import com.kelvsyc.gradle.logging.GradleLoggerDelegate
import com.kelvsyc.gradle.logging.warn
import org.gradle.api.provider.Property
import org.gradle.api.provider.ValueSource
import org.gradle.api.provider.ValueSourceParameters
import org.gradle.api.tasks.Internal

/**
 * [ValueSource] implementation that retrieves the PEM-encoded public key for an asymmetric Cloud
 * KMS crypto key version.
 *
 * Returns `null` and logs a warning when the call throws [ApiException] (e.g. the version was not
 * found, is not an asymmetric key, or the caller lacks `cloudkms.cryptoKeyVersions.viewPublicKey`
 * permission).
 */
abstract class GetPublicKeyValueSource : ValueSource<String, GetPublicKeyValueSource.Parameters> {
    companion object {
        val logger by GradleLoggerDelegate
    }

    /**
     * Parameters for [GetPublicKeyValueSource].
     */
    interface Parameters : ValueSourceParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /**
         * Fully-qualified crypto key version resource name, e.g.
         * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}/cryptoKeyVersions/{version}`.
         */
        val cryptoKeyVersionName: Property<String>
    }

    override fun obtain(): String? {
        return try {
            parameters.service.get().getClient().getPublicKey(parameters.cryptoKeyVersionName.get()).pem
        } catch (e: ApiException) {
            logger.warn(e) { "Unable to get public key for Cloud KMS version '${parameters.cryptoKeyVersionName.get()}'" }
            null
        }
    }
}
