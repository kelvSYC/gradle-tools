package com.kelvsyc.gradle.internal.aws.java.s3

import com.kelvsyc.gradle.aws.java.s3.S3TransferManagerClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import software.amazon.awssdk.transfer.s3.S3TransferManager

abstract class S3TransferManagerClientInfoInternal : S3TransferManagerClientInfo, ServiceClientInfoInternal<S3TransferManager> {
    override fun createClient(): S3TransferManager {
        return S3TransferManager.builder().apply {
            s3Client(baseClient.get())
            if (uploadDirectoryFollowSymbolicLinks.isPresent) {
                uploadDirectoryFollowSymbolicLinks(uploadDirectoryFollowSymbolicLinks.get())
            }
        }.build()
    }
}
