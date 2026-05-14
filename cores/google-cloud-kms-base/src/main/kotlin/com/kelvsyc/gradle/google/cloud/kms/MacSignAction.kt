package com.kelvsyc.gradle.google.cloud.kms

import com.google.cloud.kms.v1.MacSignRequest
import com.google.protobuf.ByteString
import org.gradle.api.file.RegularFileProperty
import org.gradle.api.provider.Property
import org.gradle.api.tasks.Internal
import org.gradle.workers.WorkAction
import org.gradle.workers.WorkParameters

/**
 * [WorkAction] implementation that computes an HMAC over the contents of [Parameters.dataFile]
 * using a Cloud KMS MAC key version and writes the resulting MAC to [Parameters.macFile].
 */
abstract class MacSignAction : WorkAction<MacSignAction.Parameters> {
    /**
     * Parameters for [MacSignAction].
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

        /** Data input file to authenticate. */
        val dataFile: RegularFileProperty

        /** MAC output file. */
        val macFile: RegularFileProperty
    }

    override fun execute() {
        val data = ByteString.copyFrom(parameters.dataFile.get().asFile.readBytes())
        val request = MacSignRequest.newBuilder()
            .setName(parameters.cryptoKeyVersionName.get())
            .setData(data)
            .build()
        val response = parameters.service.get().getClient().macSign(request)
        parameters.macFile.get().asFile.writeBytes(response.mac.toByteArray())
    }
}
