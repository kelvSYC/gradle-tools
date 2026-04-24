package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.clients.ServiceClientInfo
import software.amazon.awssdk.transfer.s3.S3TransferManager

interface MockS3TransferManagerClientInfo : ServiceClientInfo<S3TransferManager>
