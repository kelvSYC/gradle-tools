package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.model.EncryptRequest
import kotlinx.coroutines.runBlocking
import org.gradle.api.DefaultTask
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Input
import org.gradle.api.tasks.InputFile
import org.gradle.api.tasks.Internal
import org.gradle.api.tasks.OutputFile
import org.gradle.api.tasks.PathSensitive
import org.gradle.api.tasks.PathSensitivity
import org.gradle.api.tasks.TaskAction
import org.gradle.work.DisableCachingByDefault

/**
 * Task that encrypts the contents of a plaintext file under a KMS key and
 * writes the resulting ciphertext blob to disk.
 *
 * The plaintext is read as raw bytes from the [plaintextFile]; the ciphertext is written
 * as raw bytes (the `CiphertextBlob` returned by KMS, which embeds the key context required
 * for decryption).
 *
 * KMS ciphertexts are non-deterministic (random IV), so caching is disabled even though
 * input and output files are tracked.
 */
@DisableCachingByDefault(because = "KMS encryption output is non-deterministic")
abstract class Encrypt : DefaultTask() {

    /** The build service managing the KMS client. */
    @get:Internal
    abstract val service: Property<KmsClientBuildService>

    /** Key ID, ARN, or alias name (e.g. `alias/my-key`) to encrypt under. */
    @get:Input
    abstract val keyId: Property<String>

    /** Plaintext input file. */
    @get:InputFile
    @get:PathSensitive(PathSensitivity.NONE)
    abstract val plaintextFile: RegularFileProperty

    /** Ciphertext output file. */
    @get:OutputFile
    abstract val ciphertextFile: RegularFileProperty

    @TaskAction
    fun execute() = runBlocking {
        val plaintextBytes = plaintextFile.get().asFile.readBytes()
        val request = EncryptRequest {
            keyId = this@Encrypt.keyId.get()
            plaintext = plaintextBytes
        }

        val response = service.get().getClient().encrypt(request)
        ciphertextFile.get().asFile.writeBytes(response.ciphertextBlob ?: byteArrayOf())
    }
}
