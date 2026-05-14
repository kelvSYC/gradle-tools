package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.DecryptRequest
import com.google.protobuf.ByteString
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that decrypts the ciphertext blob in [Parameters.ciphertextFile]
 * using a Cloud KMS symmetric key and writes the resulting plaintext bytes to
 * [Parameters.plaintextFile].
 */
abstract class DecryptAction : WorkAction<DecryptAction.Parameters> {
    /**
     * Parameters for [DecryptAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /**
         * Fully-qualified crypto key resource name, e.g.
         * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}`.
         */
        val cryptoKeyName: Property<String>

        /** Ciphertext input file. */
        val ciphertextFile: RegularFileProperty

        /** Plaintext output file. */
        val plaintextFile: RegularFileProperty
    }

    override fun execute() {
        val ciphertext = ByteString.copyFrom(parameters.ciphertextFile.get().asFile.readBytes())
        val request = DecryptRequest.newBuilder()
            .setName(parameters.cryptoKeyName.get())
            .setCiphertext(ciphertext)
            .build()
        val response = parameters.service.get().getClient().decrypt(request)
        parameters.plaintextFile.get().asFile.writeBytes(response.plaintext.toByteArray())
    }
}
