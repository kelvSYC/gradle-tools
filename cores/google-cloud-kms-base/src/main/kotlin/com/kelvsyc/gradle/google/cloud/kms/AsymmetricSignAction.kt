package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.AsymmetricSignRequest
import com.google.cloud.kms.v1.Digest
import com.google.protobuf.ByteString
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters
import java.security.MessageDigest

/**
 * [WorkAction] implementation that signs the contents of [Parameters.dataFile] using an asymmetric
 * Cloud KMS key version and writes the resulting signature to [Parameters.signatureFile].
 *
 * The data is hashed locally using [Parameters.digestAlgorithm] before being sent to KMS as a
 * [Digest]. This is necessary for payloads that may exceed the 64 KB raw-data limit of the KMS
 * API (e.g. JAR files, release artifacts).
 *
 * [Parameters.digestAlgorithm] must be one of `"SHA256"`, `"SHA384"`, or `"SHA512"`, matching the
 * algorithm of the key version.
 */
abstract class AsymmetricSignAction : WorkAction<AsymmetricSignAction.Parameters> {
    /**
     * Parameters for [AsymmetricSignAction].
     */
    interface Parameters : WorkParameters {
        /** The build service managing the KMS client. */
        @get:Internal
        val service: Property<KmsClientBuildService>

        /**
         * Fully-qualified crypto key version resource name, e.g.
         * `projects/{project}/locations/{location}/keyRings/{keyRing}/cryptoKeys/{cryptoKey}/cryptoKeyVersions/{version}`.
         */
        val cryptoKeyVersionName: Property<String>

        /**
         * Hash algorithm to apply locally before sending to KMS. Must match the key version's
         * algorithm. Accepted values: `"SHA256"`, `"SHA384"`, `"SHA512"`.
         */
        val digestAlgorithm: Property<String>

        /** Data input file to sign. */
        val dataFile: RegularFileProperty

        /** Signature output file. */
        val signatureFile: RegularFileProperty
    }

    override fun execute() {
        val data = parameters.dataFile.get().asFile.readBytes()
        val digestBytes = MessageDigest.getInstance("SHA-${parameters.digestAlgorithm.get().removePrefix("SHA")}")
            .digest(data)
        val digest = when (parameters.digestAlgorithm.get()) {
            "SHA256" -> Digest.newBuilder().setSha256(ByteString.copyFrom(digestBytes)).build()
            "SHA384" -> Digest.newBuilder().setSha384(ByteString.copyFrom(digestBytes)).build()
            "SHA512" -> Digest.newBuilder().setSha512(ByteString.copyFrom(digestBytes)).build()
            else -> error("Unsupported digest algorithm: ${parameters.digestAlgorithm.get()}. Use SHA256, SHA384, or SHA512.")
        }
        val request = AsymmetricSignRequest.newBuilder()
            .setName(parameters.cryptoKeyVersionName.get())
            .setDigest(digest)
            .build()
        val response = parameters.service.get().getClient().asymmetricSign(request)
        parameters.signatureFile.get().asFile.writeBytes(response.signature.toByteArray())
    }
}
