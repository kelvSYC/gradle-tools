package com.kelvsyc.gradle.aws.java.kms

import com.kelvsyc.gradle.aws.java.AwsClientInfo
import software.amazon.awssdk.services.kms.KmsClient

interface KmsClientInfo : AwsClientInfo<KmsClient>
