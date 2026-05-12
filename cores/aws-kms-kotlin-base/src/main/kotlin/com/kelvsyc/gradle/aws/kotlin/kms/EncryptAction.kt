package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.model.EncryptRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that encrypts the contents of [Parameters.plaintextFile] under a KMS key and
 * writes the resulting ciphertext blob to [Parameters.ciphertextFile].
 *
 * The plaintext is read as raw bytes from disk; the ciphertext is written as raw bytes (the `CiphertextBlob`
 * returned by KMS, which embeds the key context required for decryption).
 */
abstract class EncryptAction : WorkAction<EncryptAction.Parameters> {
    /**
     * Parameters for [EncryptAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the KMS client. */
        val service: Property<KmsClientBuildService>

        /** Key ID, ARN, or alias name (e.g. `alias/my-key`) to encrypt under. */
        val keyId: Property<String>

        /** Plaintext input file. */
        val plaintextFile: RegularFileProperty

        /** Ciphertext output file. */
        val ciphertextFile: RegularFileProperty
    }

    override fun execute() {
        val plaintextBytes = parameters.plaintextFile.get().asFile.readBytes()
        val request = EncryptRequest {
            keyId = parameters.keyId.get()
            plaintext = plaintextBytes
        }

        val response = runBlocking {
            parameters.service.get().getClient().encrypt(request)
        }
        parameters.ciphertextFile.get().asFile.writeBytes(response.ciphertextBlob ?: byteArrayOf())
    }
}
