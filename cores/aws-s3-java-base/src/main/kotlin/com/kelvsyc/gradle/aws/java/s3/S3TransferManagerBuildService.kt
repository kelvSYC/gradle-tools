package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.AbstractClientBuildService
import org.gradle.api.provider.Property
import org.gradle.api.services.BuildServiceParameters
import software.amazon.awssdk.transfer.s3.S3TransferManager

/**
 * Build service managing an [S3TransferManager] in front of an [S3AsyncClientBuildService].
 *
 * Note that the underlying [S3AsyncClient][software.amazon.awssdk.services.s3.S3AsyncClient] is not closed by
 * the transfer manager (per AWS documentation); the [S3AsyncClientBuildService] owns its lifecycle.
 */
abstract class S3TransferManagerBuildService :
    AbstractClientBuildService<S3TransferManager, S3TransferManagerBuildService.Params>() {
    /**
     * Configuration parameters for [S3TransferManagerBuildService].
     */
    interface Params : BuildServiceParameters {
        /**
         * The build service supplying the underlying [S3AsyncClient][software.amazon.awssdk.services.s3.S3AsyncClient].
         */
        val baseService: Property<S3AsyncClientBuildService>

        /**
         * Whether to follow symbolic links when uploading directories. Leave unset to use the default of `false`.
         */
        val uploadDirectoryFollowSymbolicLinks: Property<Boolean>
    }

    override fun createClient(): S3TransferManager = S3TransferManager.builder().apply {
        s3Client(parameters.baseService.get().getClient())
        if (parameters.uploadDirectoryFollowSymbolicLinks.isPresent) {
            uploadDirectoryFollowSymbolicLinks(parameters.uploadDirectoryFollowSymbolicLinks.get())
        }
    }.build()
}
