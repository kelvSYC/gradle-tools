package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ServiceClientInfo
import org.gradle.api.provider.Property
import software.amazon.awssdk.services.s3.S3AsyncClient
import software.amazon.awssdk.transfer.s3.S3TransferManager

interface S3TransferManagerClientInfo : ServiceClientInfo<S3TransferManager> {
    /**
     * The underlying S3 client powering the S3 Transfer Manager client.
     *
     * Note that the underlying client is not normally closed when this client is closed, as per AWS documentation.
     * If the underlying client is a registered client created from an [S3AsyncClientInfo], the [ClientsBaseService][com.kelvsyc.gradle.clients.ClientsBaseService]
     * will close the service automatically due to its registration.
     *
     * @see [S3TransferManager.Builder.s3Client]
     */
    val baseClient: Property<S3AsyncClient>

    /**
     * Specifies whether to follow symbolic links when directories are uploaded.
     *
     * Leave unset to use the default of `false`.
     *
     * @see [S3TransferManager.Builder.uploadDirectoryFollowSymbolicLinks]
     */
    val uploadDirectoryFollowSymbolicLinks: Property<Boolean>
}
