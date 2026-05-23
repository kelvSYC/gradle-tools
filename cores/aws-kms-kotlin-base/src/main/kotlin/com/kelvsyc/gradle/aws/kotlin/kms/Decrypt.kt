package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.model.DecryptRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.Optional
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Task that decrypts a KMS ciphertext blob back into plaintext and
 * writes the resulting plaintext bytes to disk.
 *
 * For symmetric keys the key is determined from the ciphertext blob itself;
 * supplying [keyId] is optional (and is required only when the ciphertext was
 * produced under an asymmetric key).
 *
 * Caching is disabled because the decryption result depends on KMS state at
 * execution time.
 */
@DisableCachingByDefault(because = "Decryption result depends on KMS at execution time")
abstract class Decrypt : DefaultTask() {

    /** The build service managing the KMS client. */
    @get:Internal
    abstract val service: Property<KmsClientBuildService>

    /** Optional key ID, ARN, or alias name. Required only for asymmetric keys. */
    @get:Input
    @get:Optional
    abstract val keyId: Property<String>

    /** Ciphertext input file. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val ciphertextFile: RegularFileProperty

    /** Plaintext output file. */
    @get:OutputFile
    abstract val plaintextFile: RegularFileProperty

    @TaskAction
    fun execute() = runBlocking {
        val ciphertextBytes = ciphertextFile.get().asFile.readBytes()
        val request = DecryptRequest {
            keyId = this@Decrypt.keyId.orNull
            ciphertextBlob = ciphertextBytes
        }

        val response = service.get().getClient().decrypt(request)
        plaintextFile.get().asFile.writeBytes(response.plaintext ?: byteArrayOf())
    }
}
