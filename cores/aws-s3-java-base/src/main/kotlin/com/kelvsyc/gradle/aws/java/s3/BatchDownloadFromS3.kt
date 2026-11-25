package com.kelvsyc.gradle.aws.java.s3

import org.gradle.api.provider.Property
import org.gradle.api.services.ServiceReference
import org.gradle.work.DisableCachingByDefault

/**
 * An [AbstractBatchDownloadFromS3] task wired to an
 * [S3TransferManager][software.amazon.awssdk.transfer.s3.S3TransferManager] supplied by an
 * [S3TransferManagerBuildService].
 */
@DisableCachingByDefault(because = "Downloading from an external service is not cacheable")
abstract class BatchDownloadFromS3 : AbstractBatchDownloadFromS3() {
    /**
     * Build service managing the S3 transfer manager to use.
     */
    @get:ServiceReference
    abstract val service: Property<S3TransferManagerBuildService>

    init {
        client.set(service.map { it.getClient() })
        client.disallowChanges()
        client.finalizeValueOnRead()
    }
}
