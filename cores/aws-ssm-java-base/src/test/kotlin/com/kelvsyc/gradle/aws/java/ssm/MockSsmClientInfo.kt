package com.kelvsyc.gradle.aws.java.ssm

import com.kelvsyc.gradle.aws.java.AwsClientInfo
import software.amazon.awssdk.services.ssm.SsmClient

interface MockSsmClientInfo : AwsClientInfo<SsmClient>
