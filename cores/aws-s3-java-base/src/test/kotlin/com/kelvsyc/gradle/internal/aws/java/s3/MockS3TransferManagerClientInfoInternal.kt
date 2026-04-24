package com.kelvsyc.gradle.internal.aws.java.s3

import com.kelvsyc.gradle.aws.java.s3.MockS3TransferManagerClientInfo
import com.kelvsyc.gradle.clients.ServiceClientInfoInternal
import io.mockk.mockk
import software.amazon.awssdk.transfer.s3.S3TransferManager

abstract class MockS3TransferManagerClientInfoInternal :
    MockS3TransferManagerClientInfo, ServiceClientInfoInternal<S3TransferManager> {
    override fun createClient(): S3TransferManager = mockk()
}
