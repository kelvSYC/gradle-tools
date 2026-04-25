package com.kelvsyc.gradle.aws.kotlin.sns

import aws.sdk.kotlin.services.sns.SnsClient
import com.kelvsyc.gradle.aws.kotlin.AwsClientInfo

interface MockSnsClientInfo : AwsClientInfo<SnsClient>
