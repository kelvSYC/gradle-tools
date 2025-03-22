package com.kelvsyc.gradle.aws.kotlin.s3

import aws.sdk.kotlin.services.s3.S3Client
import com.kelvsyc.gradle.aws.kotlin.AwsClientInfo

interface S3ClientInfo : AwsClientInfo<S3Client>
