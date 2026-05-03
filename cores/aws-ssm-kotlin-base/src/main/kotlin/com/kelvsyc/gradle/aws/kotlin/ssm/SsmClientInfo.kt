package com.kelvsyc.gradle.aws.kotlin.ssm

import aws.sdk.kotlin.services.ssm.SsmClient
import com.kelvsyc.gradle.aws.kotlin.AwsClientInfo

interface SsmClientInfo : AwsClientInfo<SsmClient>
