package com.kelvsyc.gradle.aws.java.s3

import com.kelvsyc.gradle.aws.java.AwsClientInfo
import software.amazon.awssdk.services.s3.S3Client

interface MockS3ClientInfo : AwsClientInfo<S3Client>
