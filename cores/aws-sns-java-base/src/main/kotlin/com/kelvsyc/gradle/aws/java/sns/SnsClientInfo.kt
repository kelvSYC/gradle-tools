package com.kelvsyc.gradle.aws.java.sns

import com.kelvsyc.gradle.aws.java.AwsClientInfo
import software.amazon.awssdk.services.sns.SnsClient

interface SnsClientInfo : AwsClientInfo<SnsClient>
