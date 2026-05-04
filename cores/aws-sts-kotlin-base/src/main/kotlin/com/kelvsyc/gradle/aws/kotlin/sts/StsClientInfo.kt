package com.kelvsyc.gradle.aws.kotlin.sts

import aws.sdk.kotlin.services.sts.StsClient
import com.kelvsyc.gradle.aws.kotlin.AwsClientInfo

interface StsClientInfo : AwsClientInfo<StsClient>
