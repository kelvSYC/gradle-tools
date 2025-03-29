package com.kelvsyc.gradle.aws.java.sqs

import com.kelvsyc.gradle.aws.java.AwsClientInfo
import software.amazon.awssdk.services.sqs.SqsClient

interface SqsClientInfo : AwsClientInfo<SqsClient>
