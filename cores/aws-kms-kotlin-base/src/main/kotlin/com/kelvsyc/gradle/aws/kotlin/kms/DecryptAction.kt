package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.model.DecryptRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

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
        val request = DecryptRequest {
            keyId = parameters.keyId.orNull
            ciphertextBlob = ciphertextBytes
        }

        val response = runBlocking {
            parameters.service.get().getClient().decrypt(request)
        }
        parameters.plaintextFile.get().asFile.writeBytes(response.plaintext ?: byteArrayOf())
    }
}
