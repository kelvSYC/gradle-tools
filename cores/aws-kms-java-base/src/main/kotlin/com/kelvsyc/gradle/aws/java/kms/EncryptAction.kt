package com.kelvsyc.gradle.aws.java.kms

import com.kelvsyc.gradle.clients.ClientsBaseService
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.provider.Provider
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import software.amazon.awssdk.core.SdkBytes
import software.amazon.awssdk.services.kms.KmsClient
import software.amazon.awssdk.services.kms.model.EncryptRequest

/**
 * [WorkAction] implementation that encrypts the contents of [Parameters.plaintextFile] under a KMS key and
 * writes the resulting ciphertext blob to [Parameters.ciphertextFile].
 *
 * The plaintext is read as raw bytes from disk; the ciphertext is written as raw bytes (the `CiphertextBlob`
 * returned by KMS, which embeds the key context required for decryption).
 */
abstract class EncryptAction : WorkAction<EncryptAction.Parameters> {
    interface Parameters : WorkParameters {
        /** The shared build service managing KMS clients. */
        val service: Property<ClientsBaseService>

        /** Registered name of a [KmsClientInfo]. */
        val clientName: Property<String>

        /** Key ID, ARN, or alias name (e.g. `alias/my-key`) to encrypt under. */
        val keyId: Property<String>

        /** Plaintext input file. */
        val plaintextFile: RegularFileProperty

        /** Ciphertext output file. */
        val ciphertextFile: RegularFileProperty
    }

    private val client: Provider<KmsClient> = parameters.service.zip(parameters.clientName, ClientsBaseService::getClient)

    override fun execute() {
        val plaintextBytes = parameters.plaintextFile.get().asFile.readBytes()
        val request = EncryptRequest.builder().apply {
            keyId(parameters.keyId.get())
            plaintext(SdkBytes.fromByteArray(plaintextBytes))
        }.build()

        val response = client.get().encrypt(request)
        parameters.ciphertextFile.get().asFile.writeBytes(response.ciphertextBlob().asByteArray())
    }
}
