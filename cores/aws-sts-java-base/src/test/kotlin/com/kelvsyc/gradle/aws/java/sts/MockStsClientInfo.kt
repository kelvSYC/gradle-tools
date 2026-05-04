package com.kelvsyc.gradle.aws.java.sts

import com.kelvsyc.gradle.aws.java.AwsClientInfo
import software.amazon.awssdk.services.sts.StsClient

interface MockStsClientInfo : AwsClientInfo<StsClient>
