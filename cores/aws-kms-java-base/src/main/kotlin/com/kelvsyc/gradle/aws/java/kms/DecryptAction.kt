package com.kelvsyc.gradle.aws.java.kms

import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.kms.model.DecryptRequest

/**
 * [WorkAction] implementation that decrypts the ciphertext blob in [Parameters.ciphertextFile] using KMS and
 * writes the resulting plaintext bytes to [Parameters.plaintextFile].
 *
 * For symmetric keys the key is determined from the ciphertext blob itself; supplying [Parameters.keyId] is
 * optional (and is required only when the ciphertext was produced under an asymmetric key).
 */
abstract class DecryptAction : WorkAction<DecryptAction.Parameters> {
    /**
     * Parameters for [DecryptAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /** Optional key ID, ARN, or alias name. Required only for asymmetric keys. */
        val keyId: Property<String>

        /** Ciphertext input file. */
        val ciphertextFile: RegularFileProperty

        /** Plaintext output file. */
        val plaintextFile: RegularFileProperty
    }

    override fun execute() {
        val ciphertextBytes = parameters.ciphertextFile.get().asFile.readBytes()
        val request = DecryptRequest.builder().apply {
            if (parameters.keyId.isPresent) {
                keyId(parameters.keyId.get())
            }
            ciphertextBlob(SdkBytes.fromByteArray(ciphertextBytes))
        }.build()

        val response = parameters.service.get().getClient().decrypt(request)
        parameters.plaintextFile.get().asFile.writeBytes(response.plaintext().asByteArray())
    }
}
