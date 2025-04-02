package com.kelvsyc.gradle.aws.kotlin.ses

import aws.sdk.kotlin.services.ses.SesClient
import com.kelvsyc.gradle.aws.kotlin.AwsClientInfo

interface SesClientInfo : AwsClientInfo<SesClient>
