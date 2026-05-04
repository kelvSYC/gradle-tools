package com.kelvsyc.gradle.aws.java.lambda

import com.kelvsyc.gradle.aws.java.AwsClientInfo
import software.amazon.awssdk.services.lambda.LambdaClient

interface LambdaClientInfo : AwsClientInfo<LambdaClient>
