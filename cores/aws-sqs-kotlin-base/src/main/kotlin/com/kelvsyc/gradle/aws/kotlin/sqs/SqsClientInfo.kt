package com.kelvsyc.gradle.aws.kotlin.sqs

import aws.sdk.kotlin.services.sqs.SqsClient
import com.kelvsyc.gradle.aws.kotlin.AwsClientInfo

interface SqsClientInfo : AwsClientInfo<SqsClient>
