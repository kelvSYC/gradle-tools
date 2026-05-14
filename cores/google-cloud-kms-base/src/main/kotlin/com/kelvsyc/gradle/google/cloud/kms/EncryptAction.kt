package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.EncryptRequest
import com.google.protobuf.ByteString
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that encrypts the contents of [Parameters.plaintextFile] under a
 * Cloud KMS symmetric key and writes the resulting ciphertext blob to
 * [Parameters.ciphertextFile].
 *
 * The plaintext is read as raw bytes from disk; the ciphertext is written as the raw
 * `CiphertextBlob` returned by KMS, which embeds the key context required for decryption.
 */
abstract class EncryptAction : WorkAction<EncryptAction.Parameters> {
    /**
     * Parameters for [EncryptAction].
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

        /** Plaintext input file. */
        val plaintextFile: RegularFileProperty

        /** Ciphertext output file. */
        val ciphertextFile: RegularFileProperty
    }

    override fun execute() {
        val plaintext = ByteString.copyFrom(parameters.plaintextFile.get().asFile.readBytes())
        val request = EncryptRequest.newBuilder()
            .setName(parameters.cryptoKeyName.get())
            .setPlaintext(plaintext)
            .build()
        val response = parameters.service.get().getClient().encrypt(request)
        parameters.ciphertextFile.get().asFile.writeBytes(response.ciphertext.toByteArray())
    }
}
