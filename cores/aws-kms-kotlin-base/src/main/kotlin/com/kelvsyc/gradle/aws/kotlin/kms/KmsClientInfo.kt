package com.kelvsyc.gradle.aws.kotlin.kms

import aws.sdk.kotlin.services.kms.KmsClient
import com.kelvsyc.gradle.aws.kotlin.AwsClientInfo

interface KmsClientInfo : AwsClientInfo<KmsClient>
