package com.kelvsyc.gradle.aws.java.ses

import com.kelvsyc.gradle.aws.java.AwsClientInfo
import software.amazon.awssdk.services.ses.SesAsyncClient

interface SesAsyncClientInfo : AwsClientInfo<SesAsyncClient>
