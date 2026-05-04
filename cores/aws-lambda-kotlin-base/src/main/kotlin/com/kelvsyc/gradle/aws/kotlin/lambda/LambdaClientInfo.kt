package com.kelvsyc.gradle.aws.kotlin.lambda

import aws.sdk.kotlin.services.lambda.LambdaClient
import com.kelvsyc.gradle.aws.kotlin.AwsClientInfo

interface LambdaClientInfo : AwsClientInfo<LambdaClient>
