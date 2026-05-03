package com.kelvsyc.gradle.aws.kotlin.ecr

import aws.sdk.kotlin.services.ecr.EcrClient
import com.kelvsyc.gradle.aws.kotlin.AwsClientInfo

interface EcrClientInfo : AwsClientInfo<EcrClient>
